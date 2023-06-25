from socket import *
import pickle
from hashlib import md5
from utils import (
    file_data,
    file_exists,
    write_file,
    do_decrypt,
    do_encrypt,
)
import sys

class Peer:
    # socket server peer
    def __init__(self, c_ip = None):
        self.c_ip = c_ip
        self.sock = socket()
        self.sock.bind((self.c_ip, 0)) # porta random
        self.sock.listen(2)
        self.c_port = self.sock.getsockname()[1]

        # dados servidor de controle
        self.s_ip = '127.0.0.1'
        self.s_port = 12000

    # conecta no servidor de controle para registrar o ip e arquivo e porta do peer
    def register_peer(self):
        file = input("Digite o arquivo que deseja registrar\n")
        if(not file_exists(file)):
            print("Arquivo nao existe, tente de novo.")
            return False

        sock = socket()
        try:
            sock.connect((self.s_ip, self.s_port))
        except ConnectionRefusedError:
            sys.exit("Server de controle desligado")
        hash_file = md5(file.encode('utf-8')).hexdigest() # hash nome do arquivo
        encrypt = do_encrypt("REGISTER {} {} {}".format(hash_file, file, self.c_port))
        sock.send(encrypt) 
        resp = do_decrypt(sock.recv(1024))

        if resp == "True":
            print("Arquivo registrado com sucesso")

        sock.close()
        return True

    # procura no servidor de controle se algum peer ja registrou o dado 
    def search_file(self):
        file = input("Digite o arquivo que deseja procurar\n")
        sock = socket()
        try:
            sock.connect((self.s_ip, self.s_port))
        except ConnectionRefusedError:
            sys.exit("Server de controle desligado")
        hash_file = md5(file.encode('utf-8')).hexdigest()
        sock.send(do_encrypt("SEARCH {}".format(hash_file)))
        resp = pickle.loads(do_decrypt(sock.recv(1024)))

        if resp != None:
            sock.close()
            return resp

        sock.close()
        return False

    # baixa o arquivo do peer encontrado pelo search_file 
    def download_file(self, file, addr, port):
        sock = socket()
        try:
            sock.connect((addr, port))
        except ConnectionRefusedError:
            print("Peer desligado!")
            return False
        sock.send(do_encrypt(file))
        resp = do_decrypt(sock.recv(1024))
        sock.close()
        write_file(file, resp) # cria um arquivo com os dados baixados
        print("Download {} terminado".format(file))

        return True

    # deixa o socket server peer lendo para outros peers buscar dados
    def listen(self):
        while True:
            conn, addr = self.sock.accept()
            file = do_decrypt(conn.recv(1024))
            data = file_data(file) # le o arquivo para envia seus dados
            conn.send(do_encrypt(data))
        conn.close()
