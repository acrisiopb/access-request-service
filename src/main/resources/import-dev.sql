-- dev data
INSERT INTO tb_user (id_tb_user, tb_user_name, tb_user_email, tb_user_password_hash, tb_user_department) VALUES
  (1, 'Alice Dev', 'alice@corp.com', '$2a$10$7U/UG5V5XQoBU.DQk.UkGePoWKjrCuQwyfjHjG3r9mLThl/PNS68.', 'TI'),
  (2, 'Bruno Finance', 'bruno@corp.com', '$2a$10$vA3A5.jTbpjRkx0e5j2qXe7V7nzsXwp5Z7S0AXDhlE7yYbfo8xO6u', 'FINANCE'),
  (3, 'Carla RH', 'carla@corp.com', '$2a$10$wYcT2fQwp1rVgJjHdlV9huGKfCmwYquEzw9tCvoj57e80fRr2PrjW', 'RH'),
  (4, 'Diego Ops', 'diego@corp.com', '$2a$10$5lGz0sY4q2bcmWA7E1v9YOAtY8RR9h5xEeo6zQNG5pFZ8KNEq7g7q', 'OPERATIONS'),
  (5, 'Eva Other', 'eva@corp.com', '$2a$10$5lGz0sY4q2bcmWA7E1v9YOAtY8RR9h5xEeo6zQNG5pFZ8KNEq7g7q', 'OTHER'),
  (6, 'Test Admin', 'test@admin.com', '$2a$10$GeIOlVIbxqfzEUqFHUF5VeK/iu7GrXuSs1MNQ277JljfUP838UB5.', 'TI');

INSERT INTO tb_module (id_tb_module, tb_module_name, tb_module_description, tb_module_active) VALUES
  (1, 'PORTAL', 'Acesso geral ao portal', true),
  (2, 'RELATORIOS', 'Acesso a relatórios corporativos', true),
  (3, 'GESTAO_FINANCEIRA', 'Módulo financeiro', true),
  (4, 'APROVADOR_FINANCEIRO', 'Aprovação de finanças', true),
  (5, 'SOLICITANTE_FINANCEIRO', 'Solicitação de recursos', true),
  (6, 'ADMINISTRADOR_RH', 'Administração de recursos humanos', true),
  (7, 'COLABORADOR_RH', 'Funcionalidades básicas de RH', true),
  (8, 'ESTOQUE', 'Controle de estoque', true),
  (9, 'COMPRAS', 'Módulo compras', true),
  (10, 'AUDITORIA', 'Acesso auditoria', true);

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
  (6, 2, CURRENT_TIMESTAMP, DATEADD('DAY', 180, CURRENT_TIMESTAMP)),
  (6, 7, CURRENT_TIMESTAMP, DATEADD('DAY', 180, CURRENT_TIMESTAMP)),
  (6, 3, CURRENT_TIMESTAMP, DATEADD('DAY', 180, CURRENT_TIMESTAMP));
