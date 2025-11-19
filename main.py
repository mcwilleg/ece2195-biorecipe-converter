import os
import re
import time

import openpyxl
import progressbar
from neo4j import GraphDatabase

uri = "neo4j://localhost:7687"
username = "neo4j"
password = "password"

overwrite_existing_graph = True
input_path = "D:/University/2025 Fall/ECE2195 Knowledge Graphs/ece2195-gbm-kg/input2"

invalid_db_strings = ["none", "not applicable", "n/a", "multiple", "not specified", "none mentioned", "not available",
                      "custom", "not found", "not mentioned", "not provided", "various", "unknown"]
max_db_string_length = 20


def process_files():
    print(f"Extracting interactions from {input_path}...")
    interactions = []
    bar = progressbar.ProgressBar()
    time.sleep(0.5)
    for filename in os.listdir(input_path):
        if filename.endswith(".xlsx") and not filename.startswith("~"):
            full_path = os.path.join(input_path, filename)
            if os.path.isfile(full_path):
                wb = openpyxl.load_workbook(full_path)
                for ws in wb:
                    for row in ws.iter_rows(min_row=2, min_col=2, max_col=29):
                        i = extract_row_data(row)
                        interactions.append(i)
                        bar.update(len(interactions))
    bar.finish()
    print(f"Total interactions found: {len(interactions)}")
    time.sleep(0.5)
    nodes_added = 0
    edges_added = 0
    driver = GraphDatabase.driver(uri, auth=(username, password))
    with driver.session() as session:
        if overwrite_existing_graph:
            print("Deleting existing nodes and relationships...")
            session.run("""
            MATCH (n) DETACH DELETE n
            """)
        print("Adding interactions to database...")
        bar = progressbar.ProgressBar(max_value=len(interactions) * 2)
        bar_i = 0
        unique_db_ids = {}
        for i in interactions:
            h = i.pop("regulator")
            t = i.pop("regulated")
            h_valid = is_valid_node(h)
            t_valid = is_valid_node(t)
            if h_valid:
                for db_id in h["db_ids"]:
                    unique_db_ids[db_id] = True
            if t_valid:
                for db_id in t["db_ids"]:
                    unique_db_ids[db_id] = True
            parameters = {
                "head_label": get_valid_node_type(h["type"]),
                "head_props": h,
                "tail_label": get_valid_node_type(t["type"]),
                "tail_props": t,
                "edge_props": i,
                "edge_type": "PROMOTES" if i["sign"] == "positive" else ("INHIBITS" if i["sign"] == "negative" else "REGULATES"),
            }
            if h_valid and t_valid:
                i.pop("sign")
                session.run("""
                CREATE
                (h:$($head_label) $head_props),
                (t:$($tail_label) $tail_props),
                (h)-[:$($edge_type) $edge_props]->(t)
                FINISH
                """, parameters)
                nodes_added += 2
                edges_added += 1
            elif h_valid:
                session.run("""
                CREATE
                (h:$($head_label) $head_props)
                FINISH
                """, parameters)
                nodes_added += 1
            elif t_valid:
                session.run("""
                CREATE
                (t:$($tail_label) $tail_props)
                FINISH
                """, parameters)
                nodes_added += 1
            bar_i += 2
            bar.update(bar_i)
        bar.finish()
        print(f"Total entities created: {nodes_added}")
        print(f"Total relationships created: {edges_added}")
        time.sleep(0.5)
        print(f"Merging nodes by database IDs...")
        bar = progressbar.ProgressBar(max_value=len(unique_db_ids))
        bar_i = 0
        for db_id in unique_db_ids:
            session.run("""
            MATCH (n) WHERE $merging_id IN n.db_ids
            WITH collect(n) AS nodes
            CALL apoc.refactor.mergeNodes(nodes, {
                properties: {
                    name: 'discard',
                    other_names: 'combine',
                    db_ids: 'combine',
                    other_db_ids: 'combine'
                },
                mergeRels: true,
                singleElementAsArray: true
            })
            YIELD node
            FINISH
            """, {"merging_id": db_id})
            bar_i += 1
            bar.update(bar_i)
        bar.finish()


def is_valid_node(node):
    if not node["name"] or not is_valid_db_string(node["name"]):
        return False
    if node["db_ids"]:
        return True
    return False


def extract_row_data(row):
    clean_row(row)
    regulator = extract_node_data(row, 0)
    regulated = extract_node_data(row, 8)
    interaction = {
        "regulator": regulator,
        "regulated": regulated,
        "sign": row[16].value,
        "connection_type": row[17].value,
        "mechanism": row[18].value,
        "site": row[19].value,
        "cell_line": row[20].value,
        "cell_type": row[21].value,
        "tissue_type": row[22].value,
        "organism": row[23].value,
        "statements": row[26].value,
        "paper_ids": row[27].value,
    }
    return interaction


def extract_node_data(row, idx = 0):
    node_ids = []
    other_node_ids = []
    append_valid_db_id(node_ids, "hgnc", row[idx + 3].value)
    append_valid_db_id(other_node_ids, row[idx + 4].value, row[idx + 5].value)
    other_node_ids.extend(node_ids)
    return {
        "name": row[idx + 0].value,
        "other_names": [row[idx + 0].value],
        "type": row[idx + 1].value,
        "subtype": row[idx + 2].value,
        "db_ids": node_ids,
        "other_db_ids": other_node_ids,
        "compartment": row[idx + 6].value,
        "compartment_id": row[idx + 7].value,
    }


def append_valid_db_id(node_ids, db_name, db_id):
    if is_valid_db_string(db_name) and is_valid_db_string(db_id):
        node_id = f"{db_name}:{db_id}"
        # print(f"valid db_id: {node_id}")
        node_ids.append(node_id)


def is_valid_db_string(s):
    return (s and
            s.lower() not in invalid_db_strings and
            len(s) <= max_db_string_length and
            re.match(r"[a-zA-Z0-9-_]+", s))


def get_valid_node_type(s):
    if s and re.match(r"[a-zA-Z_-]+", s):
        return re.sub(r"[ _-]", r"", s.title()).strip()
    return "unknown"


def clean_row(a):
    for c in a:
        if not c.value:
            c.value = ""
        else:
            c.value = clean_value(c.value)


def clean_value(s):
    return s.strip()


if __name__ == '__main__':
    start_time = time.perf_counter()
    process_files()
    print(f"Completed all processes in {(time.perf_counter() - start_time):.3f} seconds.")
