from socket import *
from threading import Thread
from utils import do_decrypt, do_encrypt
import pickle # usado apenas para enviar uma lista atraves de sockets

class Server:
    def __init__(self, s_ip, s_port):
        self.s_port = s_port
        self.s_ip = s_ip
        self.sock = socket()
        self.sock.bind((self.s_ip, self.s_port))
        self.sock.listen(5)
        self.peer_lists = {}

    def search_data(self, data):
        if data in self.peer_lists:
            return self.peer_lists[data]
        return None
    
    def register_data(self, addr, data):
        addr += data
        if data[1] not in self.peer_lists:
            self.peer_lists[data[1]] = [addr]
        else: # colisao de hash
            self.peer_lists[data[1]].append(addr)
        print(self.peer_lists)

    def conn_handler(self, conn, addr):
        data = do_decrypt(conn.recv(1024))
        data = data.split()
        addr = list(addr)
        del addr[1]
        if data[0] == "REGISTER":
            self.register_data(addr, data)
            resp = "True"
            conn.send(do_encrypt(resp))
        elif data[0] == "SEARCH":
            peer = self.search_data(data[1])
            conn.send(do_encrypt(pickle.dumps(peer)))
        conn.close()

    def run(self):
        while True:
            conn, addr = self.sock.accept()
            t = Thread(target=self.conn_handler, args=[conn, addr])
            t.start()