INSERT INTO tb_user (tb_user_name, tb_user_email, tb_user_password_hash, tb_user_department) VALUES
  ('Alice Dev', 'alice@corp.com', '$2a$10$7U/UG5V5XQoBU.DQk.UkGePoWKjrCuQwyfjHjG3r9mLThl/PNS68.', 'TI'),
  ('Bruno Finance', 'bruno@corp.com', 'x', 'FINANCE'),
  ('Carla RH', 'carla@corp.com', 'x', 'RH'),
  ('Diego Ops', 'diego@corp.com', 'x', 'OPERATIONS'),
  ('Eva Other', 'eva@corp.com', 'x', 'OTHER');

INSERT INTO tb_module (tb_module_name, tb_module_description, tb_module_active) VALUES
  ('PORTAL', 'Acesso geral ao portal', true),
  ('RELATORIOS', 'Acesso a relatórios corporativos', true),
  ('GESTAO_FINANCEIRA', 'Módulo financeiro', true),
  ('APROVADOR_FINANCEIRO', 'Aprovação de finanças', true),
  ('SOLICITANTE_FINANCEIRO', 'Solicitação de recursos', true),
  ('ADMINISTRADOR_RH', 'Administração de recursos humanos', true),
  ('COLABORADOR_RH', 'Funcionalidades básicas de RH', true),
  ('ESTOQUE', 'Controle de estoque', true),
  ('COMPRAS', 'Módulo compras', true),
  ('AUDITORIA', 'Acesso auditoria', true);

-- Garantir que os IDs gerados por identidade não colidam com os registros semeados

INSERT INTO tb_module_departments (id_tb_module, department) VALUES
  (1, 'TI'), (1, 'FINANCE'), (1, 'RH'), (1, 'OPERATIONS'), (1, 'OTHER'),
  (2, 'TI'), (2, 'FINANCE'), (2, 'RH'), (2, 'OPERATIONS'), (2, 'OTHER'),
  (3, 'TI'), (3, 'FINANCE'),
  (4, 'TI'), (4, 'FINANCE'),
  (5, 'TI'), (5, 'FINANCE'),
  (6, 'TI'), (6, 'RH'),
  (7, 'TI'), (7, 'RH'),
  (8, 'TI'), (8, 'OPERATIONS'),
  (9, 'TI'), (9, 'OPERATIONS'),
  (10, 'TI');

INSERT INTO tb_module_incompatibilities (id_module, id_incompatible_module) VALUES
  (4, 5), (5, 4),
  (6, 7), (7, 6);

INSERT INTO tb_access (id_tb_user, id_tb_module, tb_access_granted_at, tb_access_expires_at) VALUES
  (2, 4, CURRENT_TIMESTAMP, DATEADD('DAY', 180, CURRENT_TIMESTAMP)),
  (3, 6, CURRENT_TIMESTAMP, DATEADD('DAY', 180, CURRENT_TIMESTAMP)),
  (4, 8, CURRENT_TIMESTAMP, DATEADD('DAY', 180, CURRENT_TIMESTAMP)),
  (4, 9, CURRENT_TIMESTAMP, DATEADD('DAY', 180, CURRENT_TIMESTAMP)),
  (1, 2, CURRENT_TIMESTAMP, DATEADD('DAY', 180, CURRENT_TIMESTAMP)),
  (5, 2, CURRENT_TIMESTAMP, DATEADD('DAY', 180, CURRENT_TIMESTAMP)),
  (1, 3, CURRENT_TIMESTAMP, DATEADD('DAY', 15, CURRENT_TIMESTAMP));