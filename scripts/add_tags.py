# add_tags.py
print("Adding 'processed' tag to all tree nodes...")

nodes = TreeNode.findAllTreeNodes(conn)
count = 0

for node in nodes:
    if not node.isRoot():
        node.setUserProp("processed", "true")
        count += 1
        print("Tagged: %s" % node.getName())

print("\nProcessed %d nodes" % count)