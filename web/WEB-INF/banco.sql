-- =========================================================
-- Banco de dados da Clínica de Estética (Sprint 3)
-- SGBD: MySQL 8.0.16+ (necessário para o CHECK funcionar)
--
-- O site roda este script sozinho na primeira vez que liga.
-- Para rodar à mão: mysql -u root -p < banco.sql
-- =========================================================

SET NAMES utf8mb4;

CREATE DATABASE IF NOT EXISTS clinica_estetica
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE clinica_estetica;

DROP TABLE IF EXISTS avaliacao;
DROP TABLE IF EXISTS agendamento;
DROP TABLE IF EXISTS servico;
DROP TABLE IF EXISTS profissional;
DROP TABLE IF EXISTS cliente;

-- ---------- Cliente ----------
-- whatsapp, celular e email: campos do formulário "Agende sua Avaliação"
CREATE TABLE cliente (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    nome_cliente VARCHAR(100) NOT NULL,
    whatsapp     VARCHAR(20),
    celular      VARCHAR(20),
    email        VARCHAR(150) NOT NULL,
    -- senha do login (criptografada, formato sal:resultado). NULL = cliente que só agendou e ainda não criou conta
    senha        VARCHAR(100),
    CONSTRAINT uq_cliente_email UNIQUE (email)
) ENGINE = InnoDB;

-- ---------- Profissional ----------
CREATE TABLE profissional (
    id                INT AUTO_INCREMENT PRIMARY KEY,
    nome_profissional VARCHAR(100) NOT NULL
) ENGINE = InnoDB;

-- ---------- Servico ----------
CREATE TABLE servico (
    id_servico   INT AUTO_INCREMENT PRIMARY KEY,
    nome_servico VARCHAR(150)   NOT NULL,
    preco        DECIMAL(10, 2) NOT NULL,
    descricao    VARCHAR(500),
    CONSTRAINT chk_servico_preco CHECK (preco >= 0)
) ENGINE = InnoDB;

-- ---------- Agendamento ----------
-- Cliente 1:N Agendamento | Profissional 1:N Agendamento | Servico 1:N Agendamento
CREATE TABLE agendamento (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    id_cliente      INT  NOT NULL,
    id_profissional INT  NOT NULL,
    id_servico      INT  NOT NULL,
    data            DATE NOT NULL,
    hora            TIME NOT NULL,
    -- um profissional não pode ter dois atendimentos no mesmo horário
    CONSTRAINT uq_agendamento_horario UNIQUE (id_profissional, data, hora),
    CONSTRAINT fk_agendamento_cliente
        FOREIGN KEY (id_cliente) REFERENCES cliente (id),
    CONSTRAINT fk_agendamento_profissional
        FOREIGN KEY (id_profissional) REFERENCES profissional (id),
    CONSTRAINT fk_agendamento_servico
        FOREIGN KEY (id_servico) REFERENCES servico (id_servico)
) ENGINE = InnoDB;

-- ---------- Avaliacao ----------
-- Cliente 1:N Avaliacao | Servico 1:N Avaliacao
-- comentario: texto do depoimento exibido na página de avaliações
CREATE TABLE avaliacao (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    nota       INT NOT NULL,
    comentario VARCHAR(500),
    id_servico INT NOT NULL,
    id_cliente INT NOT NULL,
    CONSTRAINT chk_avaliacao_nota CHECK (nota BETWEEN 0 AND 5),
    CONSTRAINT fk_avaliacao_servico
        FOREIGN KEY (id_servico) REFERENCES servico (id_servico),
    CONSTRAINT fk_avaliacao_cliente
        FOREIGN KEY (id_cliente) REFERENCES cliente (id)
) ENGINE = InnoDB;

-- =========================================================
-- Dados de exemplo
-- =========================================================
INSERT INTO cliente (nome_cliente, whatsapp, celular, email) VALUES
    ('Mariana Souza', '(11) 98888-1111', NULL,              'mariana@exemplo.com'),
    ('Juliana Rocha', '(11) 97777-2222', '(11) 97777-2222', 'juliana@exemplo.com'),
    ('Carla Mendes',  NULL,              '(11) 96666-3333', 'carla@exemplo.com');

-- Conta de demonstração para testar o login: mariana@exemplo.com / 123456
UPDATE cliente
   SET senha = 'jg11TXhsqdyeVhbcuEBCew==:aGyGp4GZuKXFtAnnxrapdY7ktwNRfEo+NSuH3bCVz4g='
 WHERE email = 'mariana@exemplo.com';

INSERT INTO profissional (nome_profissional) VALUES
    ('Ana Paula Lima'),
    ('Fernanda Costa');

INSERT INTO servico (nome_servico, preco, descricao) VALUES
    ('Limpeza de pele',           150.00, 'Remove cravos, impurezas e células mortas, deixando a pele mais saudável e luminosa. Duração: 1h.'),
    ('Peeling químico',           220.00, 'Renovação da pele com ácidos que suavizam manchas, linhas finas e marcas de acne. Duração: 1h.'),
    ('Microagulhamento',          350.00, 'Estimula a produção de colágeno para tratar cicatrizes, poros dilatados e flacidez. Duração: 1h.'),
    ('Drenagem linfática',        130.00, 'Massagem suave que reduz o inchaço e a retenção de líquidos e melhora a circulação. Duração: 1h.'),
    ('Massagem modeladora',       140.00, 'Movimentos firmes que ajudam a modelar o contorno do corpo e a reduzir medidas. Duração: 1h.'),
    ('Criolipólise',              600.00, 'Resfriamento controlado que elimina gordura localizada sem cirurgia. Duração: 1h.'),
    ('Hidratação capilar',         90.00, 'Reposição de água e nutrientes para fios ressecados, devolvendo brilho e maciez. Duração: 1h.'),
    ('Terapia capilar antiqueda', 180.00, 'Tratamento do couro cabeludo para reduzir a queda e fortalecer os fios. Duração: 1h.'),
    ('Design de sobrancelhas',     60.00, 'Modelagem das sobrancelhas de acordo com o formato do rosto, com pinça e linha. Duração: 1h.'),
    ('Depilação a laser',         200.00, 'Redução progressiva e duradoura dos pelos com laser de diodo. Preço por área. Duração: 1h.');

INSERT INTO agendamento (id_cliente, id_profissional, id_servico, data, hora) VALUES
    (1, 1, 1, '2026-12-01', '09:00'),
    (2, 2, 4, '2026-12-01', '09:00'),
    (3, 2, 7, '2026-12-02', '14:00');

INSERT INTO avaliacao (nota, comentario, id_servico, id_cliente) VALUES
    (5, 'Atendimento impecável! Minha pele nunca esteve tão bonita.',        1, 1),
    (5, 'Ambiente acolhedor e profissionais muito atenciosos. Recomendo!', 4, 2),
    (4, 'Fiz drenagem e já senti diferença na primeira sessão.',          4, 3);
