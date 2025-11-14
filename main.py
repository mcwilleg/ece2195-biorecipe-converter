import datetime
import json
import os
import re
import time

import openpyxl

input_path = "D:/University/2025 Fall/ECE2195 Knowledge Graphs/ece2195-gbm-kg/input2"

valid_node_types = ["protein", "gene", "chemical", "RNA", "protein family", "biological process"]

last_uid = 0

hgnc_dict = {}
db_dict = {}
head_edge_dict = {}
tail_edge_dict = {}

node_dict = {}
edge_dict = {}

def process_files():
    start_time = time.perf_counter()
    interactions = []
    for filename in os.listdir(input_path):
        if filename.endswith(".xlsx") and not filename.startswith("~"):
            full_path = os.path.join(input_path, filename)
            if os.path.isfile(full_path):
                wb = openpyxl.load_workbook(full_path)
                for ws in wb:
                    for row in ws.iter_rows(min_row=2, min_col=2, max_col=29):
                        interactions.append(extract_row_data(row))
    elapsed_time = time.perf_counter() - start_time
    print(f"File extraction completed in {elapsed_time:.3f} seconds.")
    for i in interactions:
        h = i["regulator"]
        t = i["regulated"]
        h_valid = is_valid_node(h)
        t_valid = is_valid_node(t)
        if h_valid:
            save_node(h)
        if t_valid:
            save_node(t)
        if h_valid and t_valid:
            save_edge(i)
    condense_nodes()
    elapsed_time = time.perf_counter() - start_time
    print(f"Interactions: {len(interactions)}, (Max Unique Nodes: {len(interactions) * 2})")
    print(f"Unique Nodes: {len(node_dict)}")
    print(f"Unique Edges: {len(edge_dict)}")
    print(f"Completed in {elapsed_time:.3f} seconds.")


def write_cypher_queries():
    timestamp = datetime.datetime.now().strftime("%Y%m%d-%H%M%S")
    output_path = f"{input_path}/output"
    os.makedirs(output_path, exist_ok=True)
    file_name = f"cypher_queries_{timestamp}.txt"
    with open(f"{output_path}/{file_name}", "w") as f:
        f.write("CREATE\n")
        node_i = 0
        for node in node_dict:
            node_query = get_create_node_cypher_query(node_dict[node])
            if 0 < node_i < len(node_dict):
                f.write(",\n")
            f.write(f"{node_query}")
            node_i += 1
        f.write(";\n")
        for edge in edge_dict:
            edge_query = get_create_edge_cypher_query(edge_dict[edge])
            f.write(f"{edge_query};\n")


def get_create_node_cypher_query(node):
    uid = node["uid"]
    clean_obj_for_query(node)
    property_map = json.dumps(node)
    property_map = re.sub(r"\"([a-z_]+)\": ", r"\1: ", property_map)
    property_map = re.sub(r"(: \")", r": '", property_map)
    property_map = re.sub(r"(\",)", r"',", property_map)
    property_map = re.sub(r"(\"})", r"'}", property_map)
    return f"({uid}:Node {property_map})"


def get_create_edge_cypher_query(edge):
    h = edge.pop("head")
    t = edge.pop("tail")
    edge.pop("uid")
    clean_obj_for_query(edge)
    property_map = json.dumps(edge)
    property_map = re.sub(r"\"([a-z_]+)\": ", r"\1: ", property_map)
    return f"MATCH (h:Node) MATCH (t:Node) WHERE h.uid = \'{h}\' AND t.uid = \'{t}\' MERGE (h)-[:REGULATES {property_map}]->(t) FINISH"
    # return f"CREATE ({h})-[:REGULATES {property_map}]->({t})"


def clean_obj_for_query(obj):
    for key in obj:
        value = obj[key]
        if value:
            value = value.replace("'", "")
            value = value.replace('"', "")
        obj[key] = value


def get_valid_node_type(node_type):
    if node_type:
        node_type = node_type.lower()
        if node_type in valid_node_types:
            return str(node_type).replace(" ", "_")
    return "unknown"


def save_node(node):
    if node["hgnc_symbol"]:
        existing = hgnc_dict.get(node["hgnc_symbol"])
        if existing:
            hgnc_dict[node["hgnc_symbol"]] = merge_nodes(existing, node)
        else:
            hgnc_dict[node["hgnc_symbol"]] = node
    if node["db_source"] and node["db_id"]:
        key = get_db_key(node)
        existing = db_dict.get(key)
        if existing:
            db_dict[key] = merge_nodes(existing, node)
        else:
            db_dict[key] = node


def condense_nodes():
    for key in hgnc_dict:
        node = hgnc_dict[key]
        db_key = get_db_key(node)
        if db_key in db_dict:
            db_node = db_dict.pop(db_key)
            hgnc_dict[key] = merge_nodes(node, db_node)
    for node in hgnc_dict:
        node_dict[hgnc_dict[node]["uid"]] = hgnc_dict[node]
    for node in db_dict:
        node_dict[db_dict[node]["uid"]] = db_dict[node]


def save_edge(i):
    h = i["regulator"]["uid"]
    t = i["regulated"]["uid"]
    uid = generate_uid()
    edge_data = {
        "uid": uid,
        "head": h,
        "tail": t,
    }
    for p in ["sign", "connection_type", "mechanism", "site", "cell_line", "cell_type", "tissue_type", "organism", "paper_ids",]:
        edge_data[p] = i[p]
    edge_dict[uid] = edge_data
    head_edges = head_edge_dict.get(h, [])
    head_edges.append(uid)
    head_edge_dict[h] = head_edges
    tail_edges = tail_edge_dict.get(t, [])
    tail_edges.append(uid)
    tail_edge_dict[t] = tail_edges


def merge_nodes(original, new):
    if not original["hgnc_symbol"]:
        original["hgnc_symbol"] = new["hgnc_symbol"]
    if not original["db_source"] or not original["db_id"]:
        original["db_source"] = new["db_source"]
        original["db_id"] = new["db_id"]
    if not original["type"]:
        original["type"] = new["type"]
    if not original["subtype"]:
        original["subtype"] = new["subtype"]
    if not original["compartment"]:
        original["compartment"] = new["compartment"]
    if not original["compartment_id"]:
        original["compartment_id"] = new["compartment_id"]
    original_id = original["uid"]
    new_id = new["uid"]
    if new_id in head_edge_dict:
        head_edges = head_edge_dict[new_id]
        for e in head_edges:
            edge_data = edge_dict[e]
            edge_data["head"] = original_id
    if new_id in tail_edge_dict:
        tail_edges = tail_edge_dict[new_id]
        for e in tail_edges:
            edge_data = edge_dict[e]
            edge_data["tail"] = original_id
    return original


def is_valid_node(node):
    if not node["name"]:
        return False
    if has_valid_hgnc_symbol(node) or has_valid_db_key(node):
        return True
    return False


def has_valid_hgnc_symbol(node):
    return node["hgnc_symbol"] and re.match(r"[\w ]+", node["hgnc_symbol"])


def has_valid_db_key(node):
    return node["db_source"] and node["db_id"] and re.match(r"[\w ]+:[\w ]+", get_db_key(node))


def extract_row_data(row):
    regulator = {
        "name": row[0].value,
        "uid": generate_uid(),
        "type": row[1].value,
        "subtype": row[2].value,
        "hgnc_symbol": row[3].value,
        "db_source": row[4].value,
        "db_id": row[5].value,
        "compartment": row[6].value,
        "compartment_id": row[7].value,
    }
    clean_values(regulator)
    regulated = {
        "name": row[8].value,
        "uid": generate_uid(),
        "type": row[9].value,
        "subtype": row[10].value,
        "hgnc_symbol": row[11].value,
        "db_source": row[12].value,
        "db_id": row[13].value,
        "compartment": row[14].value,
        "compartment_id": row[15].value,
    }
    clean_values(regulated)
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
    clean_values(interaction)
    return interaction


def get_db_key(node):
    return f"{node["db_source"]}:{node["db_id"]}"


def generate_uid():
    global last_uid
    uid = f"x{str(last_uid)}"
    last_uid += 1
    return uid


def clean_values(d):
    for key, value in d.items():
        if type(value) == "<class 'str'>":
            d[key] = clean_value(value)
        if not value:
            d[key] = ""


def clean_value(s):
    return s.strip()


if __name__ == '__main__':
    last_uid = 0
    process_files()
    write_cypher_queries()
