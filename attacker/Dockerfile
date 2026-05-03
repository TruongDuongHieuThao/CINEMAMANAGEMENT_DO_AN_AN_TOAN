FROM nginx:alpine

# Copy static HTML files
COPY html/ /usr/share/nginx/html/

# Copy nginx config
COPY nginx.conf /etc/nginx/nginx.conf

EXPOSE 4000

CMD ["nginx", "-g", "daemon off;"]