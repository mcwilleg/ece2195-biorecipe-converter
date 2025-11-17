import json
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

valid_node_types = ["protein", "gene", "chemical", "RNA", "protein family", "biological process"]
invalid_db_strings = ["none", "not applicable", "n/a", "multiple", "not specified", "none mentioned", "not available",
                      "custom", "not found", "not mentioned", "not provided", "various", "unknown"]
max_db_string_length = 20

last_uid = 0

hgnc_dict = {}
db_dict = {}
head_edge_dict = {}
tail_edge_dict = {}

node_dict = {}
edge_dict = {}

def process_files():
    print(f"Extracting interactions from {input_path}...")
    interactions = []
    bar = progressbar.ProgressBar()
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
            if h_valid and t_valid:
                parameters = {
                        "head_name": h["name"],
                        "head_db_ids": h["db_ids"],
                        "tail_name": t["name"],
                        "tail_db_ids": t["db_ids"],
                        "edge_props": i,
                }
                sign = i.pop("sign")
                if sign == "positive":
                    session.run("""
                    CREATE
                    (h:Node {name: $head_name, db_ids: $head_db_ids}),
                    (t:Node {name: $tail_name, db_ids: $tail_db_ids}),
                    (h)-[:PROMOTES $edge_props]->(t)
                    FINISH
                    """, parameters)
                elif sign == "negative":
                    session.run("""
                    CREATE
                    (h:Node {name: $head_name, db_ids: $head_db_ids}),
                    (t:Node {name: $tail_name, db_ids: $tail_db_ids}),
                    (h)-[:INHIBITS $edge_props]->(t)
                    FINISH
                    """, parameters)
                nodes_added += 2
                edges_added += 1
            elif h_valid:
                session.run("""
                CREATE
                (h:Node {name: $name, db_ids: $db_ids})
                FINISH
                """, h)
                nodes_added += 1
            elif t_valid:
                session.run("""
                CREATE
                (t:Node {name: $name, db_ids: $db_ids})
                FINISH
                """, t)
                nodes_added += 1
            bar_i += 2
            bar.update(bar_i)
        bar.finish()
        print(f"Total entities created: {nodes_added}")
        print(f"Total relationships created: {edges_added}")
        print(f"Merging nodes by database IDs...")
        bar = progressbar.ProgressBar(max_value=len(unique_db_ids))
        bar_i = 0
        for db_id in unique_db_ids:
            session.run("""
            MATCH (n) WHERE $merging_id IN n.db_ids
            WITH collect(n) AS nodes
            CALL apoc.refactor.mergeNodes(nodes, {
                properties: {
                    name: 'combine',
                    db_ids: 'combine'
                },
                mergeRels: true,
                produceSelfRel: false
            })
            YIELD node
            FINISH
            """, {"merging_id": db_id})
            bar_i += 1
            bar.update(bar_i)
        bar.finish()


def is_valid_node(node):
    if not node["name"]:
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
    # append_valid_db_id(node_ids, "hgnc", row[idx + 3].value)
    append_valid_db_id(node_ids, row[idx + 4].value, row[idx + 5].value)
    return {
        "name": row[idx + 0].value,
        "type": row[idx + 1].value,
        "subtype": row[idx + 2].value,
        "hgnc_symbol": row[idx + 3].value,
        "db_ids": node_ids,
        "compartment": row[idx + 6].value,
        "compartment_id": row[idx + 7].value,
    }


def append_valid_db_id(node_ids, db_name, db_id):
    if is_valid_db_string(db_name) and is_valid_db_string(db_id):
        node_ids.append(f"{db_name}:{db_id}")


def is_valid_db_string(s):
    return s and (s.lower() not in invalid_db_strings) and len(s) <= max_db_string_length and re.match(r"\w+", s)


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
    last_uid = 0
    process_files()
    print(f"Completed all processes in {(time.perf_counter() - start_time):.3f} seconds.")
