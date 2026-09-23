# These parameters will be created with dummy values initially.
# You must update them manually in the AWS Console (or via CLI) with the real production secrets.

resource "aws_ssm_parameter" "db_password" {
  name        = "/${var.project_name}/${var.environment}/database_password"
  description = "PostgreSQL password for the application user"
  type        = "SecureString"
  value       = "CHANGE_ME_IN_AWS_CONSOLE"

  lifecycle {
    ignore_changes = [value]
  }
}

resource "aws_ssm_parameter" "jwt_secret" {
  name        = "/${var.project_name}/${var.environment}/jwt_secret"
  description = "JWT Secret for authentication"
  type        = "SecureString"
  value       = "CHANGE_ME_IN_AWS_CONSOLE"

  lifecycle {
    ignore_changes = [value]
  }
}

resource "aws_ssm_parameter" "judge0_api_key" {
  name        = "/${var.project_name}/${var.environment}/judge0_api_key"
  description = "Judge0 RapidAPI Key"
  type        = "SecureString"
  value       = "CHANGE_ME_IN_AWS_CONSOLE"

  lifecycle {
    ignore_changes = [value]
  }
}

resource "aws_ssm_parameter" "gemini_api_key" {
  name        = "/${var.project_name}/${var.environment}/gemini_api_key"
  description = "Gemini API Key"
  type        = "SecureString"
  value       = "CHANGE_ME_IN_AWS_CONSOLE"

  lifecycle {
    ignore_changes = [value]
  }
}

resource "aws_ssm_parameter" "admin_password" {
  name        = "/${var.project_name}/${var.environment}/admin_password"
  description = "Admin Bootstrap Password"
  type        = "SecureString"
  value       = "CHANGE_ME_IN_AWS_CONSOLE"

  lifecycle {
    ignore_changes = [value]
  }
}
