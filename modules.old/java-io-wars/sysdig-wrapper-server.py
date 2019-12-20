import socketserver

class SysdigWrapper(socketserver.BaseRequestHandler):
    def handle(self):
        data = self.request.recv(1024).strip()
        print("recevied %s" % data)


if __name__=="__main__":
    tcp_server = socketserver.TCPServer(("localhost",9999),SysdigWrapper)
    tcp_server.serve_forever()
