from server import Server
from peer import Peer
from threading import Thread

# C:\Users\Bira\Projects\SD-EP-Napster\napster-p2p\storage-1

choice = input("1 -> Server, 2 -> Peer\n")
if choice == "1":
    print("Starting Server")

    s_IP = input("Enter the server IP: ")
    if s_IP == "":
        s_IP = '127.0.0.1'

    s_port = input("Enter the server port: ")
    if s_port == "":
        s_port = 1099
    else:
        s_port = int(s_port)

    server = Server(s_IP, s_port)
    server.run()

elif choice == "2":
    print("Starting Peer")

    p_IP = input("Enter the peer IP: ")
    if p_IP == "":
        p_IP = '127.0.0.1'

    p_port = input("Enter the peer port: ")
    if p_port == "":
        p_port = 0
    else:
        p_port = int(p_port)

    p_storage = input("Enter the peer storage: ")
    if p_storage == "":
        p_storage = "C:/Users\Bira\Projects\SD-EP-Napster\napster-p2p\storage"

    peer = Peer(p_IP, p_port, p_storage)
    t = Thread(target = peer.listen, args = ())
    t.start()
    while True:
        choice2 = input("1 -> JOIN, 2 -> SEARCH, 3 -> DOWNLOAD\n")
        if choice2 == "1":
            peer.register()
        elif choice2 == "2":
            file = input("Enter the file name: ")
            peer.search(file)
        elif choice2 == "3":
            p_IP = input("Enter the peer IP: ")
            p_port = int(input("Enter the peer port: "))
            file = input("Enter the file name: ")
            peer.download(p_IP, p_port, file)

else:
    print("Ending proccess")
    exit()