from http.server import HTTPServer, BaseHTTPRequestHandler
from urllib.parse import urlparse
import json

class GetHandler(BaseHTTPRequestHandler):

    def do_GET(self):
        parsed_path = urlparse(self.path)
        qp = dict(qc.split("=") for qc in parsed_path.query.split("&"))
        
        if "operation" in qp and "t1" in qp and "t2" in qp:
            t1 = int(qp["t1"])
            t2 = int(qp["t2"])
            if qp["operation"] == "plus":
                message = "%s\n" % (t1 + t2) 
            if qp["operation"] == "minus":
                message = "%s\n" % (t1 - t2) 
            if qp["operation"] == "times":
                message = "%s\n" % (t1 * t2) 
            if qp["operation"] == "divide" and abs(t2) > 1e-10:
                message = "%s\n" % (t1 / t2) 
            
            self.send_response(200)
            self.end_headers()
            self.wfile.write(message.encode('utf-8'))
        return


if __name__ == '__main__':
    server = HTTPServer(('0.0.0.0', 8080), GetHandler)
    print('Starting server at http://localhost:8080')
    server.serve_forever()