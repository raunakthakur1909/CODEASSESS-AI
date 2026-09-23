terraform {
  required_version = ">= 1.5.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }

  # Note: For production, uncomment and configure the S3 backend
  # backend "s3" {
  #   bucket         = "codeassess-terraform-state"
  #   key            = "prod/terraform.tfstate"
  #   region         = "us-east-1"
  #   dynamodb_table = "codeassess-terraform-locks"
  #   encrypt        = true
  # }
}

provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Project     = "CodeAssess AI"
      Environment = var.environment
      ManagedBy   = "Terraform"
    }
  }
}
