# update_node.py
if uuid is None:
    print("Error: This script requires a specific UUID")
else:
    print("Updating node: %s" % uuid)
    
    node = Node(conn, uuid)
    
    # Add a timestamp property
    import time
    timestamp = time.strftime("%Y-%m-%d %H:%M:%S")
    node.setUserProp("script_executed", timestamp)
    node.setUserProp("script_name", "update_node.py")
    
    print("Added properties to node '%s'" % node.getUuid())
    print("Success!")