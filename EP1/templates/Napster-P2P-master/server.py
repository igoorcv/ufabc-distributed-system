import socket
from threading import Thread

class Server:
    def __init__(self, IP, port):
        self.IP = IP
        self.port = port
        self.s = socket.socket()
        self.s.bind((self.IP, self.port))
        self.s.listen(5)
        print("Server started at IP: " + self.IP + " Port: " + str(self.port))
        self.peers = {}

    def run(self):
        while True:
            c, addr = self.s.accept()
            t = Thread(target = self.peer_handler, args = (c, addr))
            t.start()

    def peer_handler(self, c, addr):
        request = c.recv(1024).decode()
        data = request.split()
        if data[0] == "JOIN":
            files = ' '.join(data[3:]).replace('[', '').replace(']', '').replace('"', '').replace(',', '').replace("'", '')
            self.peers[data[2]] = (data[1], data[2], data[3:])
            print('Peer {}:{} adicionado com arquivos {}'.format(data[1], data[2], files))
            response = 'JOIN_OK'
            c.send("{}".format(response).encode())
            c.close()

        elif data[0] == "SEARCH":
            data = data[1]
            p_IP = addr[0]
            p_port = addr[1]
            print('Peer {}:{} solicitou arquivo {}'.format(p_IP, p_port, data))
            response = "SEARCH_OK"
            matching_peers = []
            for peer_data in self.peers.values():
                files = peer_data[2]
                if data in files:
                    peer = peer_data[0] + ":" + peer_data[1]
                    matching_peers.append(peer)  # Adiciona o peer compatível à lista
            response_data = {
                "matching_peers": matching_peers,
                "response": response
            }
            c.send(str(response_data).encode())
            c.close()
        
        elif data[0] == "UPDATE":
            array = data[3:]
            self.peers[data[2]] = (data[1], data[2], array)
            response = 'UPDATE_OK'
            c.send("{}".format(response).encode())
            c.close()
