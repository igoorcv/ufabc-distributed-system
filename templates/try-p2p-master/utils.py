import os
from Crypto.Cipher import AES

ROOT_DIR = os.path.dirname(os.path.abspath(__file__))

# chaves iguais para server e peer
counter = b'\xc3\x9e\xfe\xf1{$\xa5\x85\xc2\x9f\x0c\xe5\x82\xc5\x1bq'
key = b'\xdf-\xd5\xd1y\xb0\xe8\x02/b0QQ\xfag~\x1bE\xf9\r\xb5L\xf2\x9c\xfe\xb5Q\xff\xce^]\xf8'

# retorna os dados de um arquivo
def file_data(file):
    path = ROOT_DIR + '/upload_files/{}'.format(file)
    if os.path.isfile(path):
        data = open(path, 'r')
        data = data.read()
        return data     

# verifica se um arquivo existe
def file_exists(file):
    path = ROOT_DIR + '/upload_files/{}'.format(file)
    if os.path.isfile(path):
        return True
    return False

# cria um novo arquivo com os dados baixados
def write_file(name, data):
    path = os.path.join(ROOT_DIR, 'download_files')
    if not os.path.exists(path):
        os.makedirs(path)
    file = open(path+'/'+name, 'a+')
    file.write(data)
    file.close()

# criptografa
def do_encrypt(message):
    enc = AES.new(key, AES.MODE_CTR, counter=lambda: counter)
    encrypted = enc.encrypt(message)
    return encrypted

# descriptografa
def do_decrypt(ciphertext):
    dec = AES.new(key, AES.MODE_CTR, counter=lambda: counter)
    decrypted = dec.decrypt(ciphertext)

    try:
        decrypted = decrypted.decode('utf-8')
    except UnicodeDecodeError: # caso do pickle para enviar uma estrutura no socket
        pass

    return decrypted
