import socket
import os


class Peer:
    def __init__(self, IP, port, storage, s_IP = '127.0.0.1', s_port = 1099 ):
        self.IP = IP
        self.port = port
        self.storage = storage
        self.s = socket.socket()
        self.s.bind((self.IP, 0)) # random port
        self.s.listen(2)
        self.port = self.s.getsockname()[1]
        print("Peer started at IP: " + self.IP + " Port: " + str(self.port))

        self.s_IP = s_IP
        self.s_port = s_port

    def listen(self):
        while True:
            c, addr = self.s.accept()
            data = c.recv(1024).decode()
            data = data.split()
            if data[0] == "DOWNLOAD":
                file = data[1]
                print("Peer {}:{} solicitou arquivo {}".format(addr[0], addr[1], file))
                self.send_file(c, file)
            c.close()

    def register(self):
        s = socket.socket()
        s.connect((self.s_IP, self.s_port))
        files = self.get_my_files()
        files = ' '.join(files).replace('[', '').replace(']', '').replace('"', '').replace(',', '').replace("'", '')
        data = "JOIN {} {} {}".format(self.IP, str(self.port), files)
        s.send(data.encode())
        response = s.recv(1024).decode()
        if response == "JOIN_OK":
            print("Sou peer {}:{} com arquivos {}".format(self.IP, self.port, files))
        s.close()

    def search(self, file):
        s = socket.socket()
        s.connect((self.s_IP, self.s_port))
        data = "SEARCH {}".format(file)
        s.send(data.encode())
        response = s.recv(1024).decode()
        response = eval(response)
        matching_peers = response['matching_peers']
        response = response['response']
        if response == "SEARCH_OK":
            data = ' '.join(matching_peers).replace('[', '').replace(']', '').replace('"', '').replace(',', '').replace("'", '')
            print("peers com arquivo solicitado: " + data)
        s.close()

    def get_my_files(self):
        file_names = []
        for file in os.listdir(self.storage):
            file_names.append(file)
        return file_names
    
    def download(self, p_IP, p_port, file):
        s = socket.socket()
        s.connect((p_IP, p_port))
        data = "DOWNLOAD {}".format(file)
        s.send(data.encode())
        response = s.recv(1024).decode()
        self.receive_file(s, response, file)
        s.close()

    def send_file(self, c, file):
        try:
            with open(os.path.join(self.storage, file), 'rb') as f:
                c.send("DOWNLOAD_OK".encode())
                data = f.read(1024)
                while data:
                    c.send(data)
                    data = f.read(1024)
        except:
            c.send("DOWNLOAD_FAIL".encode())
        c.close()
    
    def receive_file(self, s, response, file):
        if response == "DOWNLOAD_OK":
            with open(os.path.join(self.storage, file), 'wb') as f:
                data = s.recv(1024)
                while data:
                    f.write(data)
                    data = s.recv(1024)
            print("Arquivo {} baixado com sucesso na pasta {}".format(file, self.storage))
            self.update()
        else:
            print("Falha ao receber arquivo {}".format(file))
        s.close()

    def update(self):
        s = socket.socket()
        s.connect((self.s_IP, self.s_port))
        files = self.get_my_files()
        files = ' '.join(files).replace('[', '').replace(']', '').replace('"', '').replace(',', '').replace("'", '')
        data = "UPDATE {} {} {}".format(self.IP, str(self.port), files)
        s.send(data.encode())
        response = s.recv(1024).decode()
        if response == "UPDATE_OK":
            s.close()
        else:
            print("Falha ao atualizar arquivos")
            s.close()
