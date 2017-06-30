# curri-gate

The gateway to the backend has two responsibilities:
- authenticate
- pass requests to services in the backend

## To Run

Run the main in `CurriGateApplication`. Start the front end, integrate with nginx:

    server {
        listen 8001;
        server_name microdocs.xip.io;
    
        location / {
          proxy_set_header Host $host;
          proxy_pass http://localhost:3000;
        }
        location /service {
          proxy_set_header Host $host;
          proxy_pass http://localhost:8080;
        }
        location /login {
          proxy_set_header Host $host;
          proxy_pass http://localhost:8080;
        }
        location /logout {
          proxy_set_header Host $host;
          proxy_pass http://localhost:8080;
        }
      }