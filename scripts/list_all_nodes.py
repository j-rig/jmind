# list_all_nodes.py
print("Listing all tree nodes in database:")
print("=" * 50)

nodes = TreeNode.findAllTreeNodes(conn)
print("Found %d nodes" % len(nodes))

for node in nodes:
    print("\nUUID: %s" % node.getUuid())
    print("Name: %s" % node.getName())
    print("Parent: %s" % node.getParentUuid())
    print("-" * 30)