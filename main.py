import os
import time
import uuid
import openpyxl

input_path = "D:/University/2025 Fall/ECE2195 Knowledge Graphs/ece2195-gbm-kg/input2"

hgnc_dict = {}
db_dict = {}
edge_dict = {}
head_edge_dict = {}
tail_edge_dict = {}

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
    print(f"Unique Nodes: {len(hgnc_dict) + len(db_dict)}")
    print(f"Unique Edges: {len(edge_dict)}")
    print(f"Completed in {elapsed_time:.3f} seconds.")


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
    for key in hgnc_dict.keys():
        node = hgnc_dict[key]
        db_key = get_db_key(node)
        if db_key in db_dict.keys():
            db_node = db_dict.pop(db_key)
            hgnc_dict[key] = merge_nodes(node, db_node)


def save_edge(i):
    h = i["regulator"]["uid"]
    t = i["regulated"]["uid"]
    uid = generate_uid()
    edge_data = {
        "uid": uid,
        "head": h,
        "tail": t,
    }
    edge_dict[uid] = edge_data
    head_edges = head_edge_dict.get(h, [])
    head_edges.append(uid)
    tail_edges = tail_edge_dict.get(t, [])
    tail_edges.append(uid)


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
    head_edges = head_edge_dict.get(new_id, [])
    for e in head_edges:
        edge_data = edge_dict[e]
        edge_data["head"] = original_id
    tail_edges = tail_edge_dict.get(new_id, [])
    for e in tail_edges:
        edge_data = edge_dict[e]
        edge_data["tail"] = original_id
    return original


def is_valid_node(node):
    if not node["name"]:
        return False
    if node["hgnc_symbol"]:
        return True
    elif node["db_source"] and node["db_id"]:
        return True
    return False


def extract_row_data(row):
    regulator = {
        "uid": generate_uid(),
        "name": row[0].value,
        "type": row[1].value,
        "subtype": row[2].value,
        "hgnc_symbol": row[3].value,
        "db_source": row[4].value,
        "db_id": row[5].value,
        "compartment": row[6].value,
        "compartment_id": row[7].value,
    }
    strip_values(regulator)
    regulated = {
        "uid": generate_uid(),
        "name": row[8].value,
        "type": row[9].value,
        "subtype": row[10].value,
        "hgnc_symbol": row[11].value,
        "db_source": row[12].value,
        "db_id": row[13].value,
        "compartment": row[14].value,
        "compartment_id": row[15].value,
    }
    strip_values(regulated)
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
    strip_values(interaction)
    return interaction


def get_db_key(node):
    return f"{node["db_source"]}:{node["db_id"]}"


def generate_uid():
    return f"x{str(uuid.uuid4()).replace("-", "")}"


def strip_values(d):
    for key, value in d.items():
        if type(value) == "<class 'str'>":
            d[key] = value.strip()


if __name__ == '__main__':
    process_files()
