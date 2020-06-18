from os import environ as env

# Gunicorn Configuration
bind = ":" + env.get("PORT", "8080")
workers = int(env.get("WORKERS", 1))
threads = int(env.get("HTTP_THREADS", 1))

