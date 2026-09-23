resource "aws_db_subnet_group" "rds_subnet_group" {
  name       = "${var.project_name}-rds-subnet-group-${var.environment}"
  subnet_ids = aws_subnet.private[*].id

  tags = {
    Name = "${var.project_name}-rds-subnet-group-${var.environment}"
  }
}

resource "aws_db_instance" "postgres" {
  identifier           = "${var.project_name}-postgres-${var.environment}"
  engine               = "postgres"
  engine_version       = "15.7" # Assuming 15, adjust based on AWS availability if needed (user uses 18 in Docker, but RDS max is typically 15 or 16)
  instance_class       = var.db_instance_class
  allocated_storage    = 20
  storage_type         = "gp3"
  
  db_name              = "codeassess_ai"
  username             = "codeassess_app" # We can just use the app user as the master user for simplicity, or create a separate master user.
  password             = aws_ssm_parameter.db_password.value

  db_subnet_group_name   = aws_db_subnet_group.rds_subnet_group.name
  vpc_security_group_ids = [aws_security_group.rds.id]

  backup_retention_period = 7
  backup_window           = "03:00-04:00"
  maintenance_window      = "Mon:04:00-Mon:05:00"

  skip_final_snapshot     = true
  publicly_accessible     = false

  tags = {
    Name = "${var.project_name}-rds-${var.environment}"
  }
}
