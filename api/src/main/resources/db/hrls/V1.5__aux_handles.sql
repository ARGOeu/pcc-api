CREATE TABLE `aux_handles` (
  `handle` varchar(255)  COLLATE utf8mb4_unicode_ci NOT NULL,
  is_resolved int(1) DEFAULT 0 NOT NULL
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
