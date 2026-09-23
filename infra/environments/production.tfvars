aws_region        = "us-east-1"
environment       = "production"
project_name      = "codeassess"
vpc_cidr          = "10.0.0.0/16"
db_instance_class = "db.t3.micro"

# These should be overridden by CI/CD during deployment with the actual GHCR image SHAs
backend_image     = "ghcr.io/your-github-user/codeassess-ai-backend:latest"
frontend_image    = "ghcr.io/your-github-user/codeassess-ai-frontend:latest"

# Optional: Provide if you have a registered domain and ACM certificate
# domain_name       = "app.codeassess.local"
# certificate_arn   = "arn:aws:acm:us-east-1:123456789012:certificate/xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
