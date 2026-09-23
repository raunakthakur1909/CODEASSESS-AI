output "alb_dns_name" {
  description = "The DNS name of the load balancer"
  value       = aws_lb.main.dns_name
}

output "ecs_cluster_name" {
  description = "The name of the ECS cluster"
  value       = aws_ecs_cluster.main.name
}

output "backend_service_name" {
  description = "The name of the Backend ECS service"
  value       = aws_ecs_service.backend.name
}

output "frontend_service_name" {
  description = "The name of the Frontend ECS service"
  value       = aws_ecs_service.frontend.name
}

output "rds_endpoint" {
  description = "The connection endpoint for the RDS instance"
  value       = aws_db_instance.postgres.endpoint
}
