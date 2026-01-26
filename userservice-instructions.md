# 1. USER SERVICE

**Classe:** `UserService`

## Dependências Injetadas

- `UserRepository`
- `PasswordEncoder` (BCryptPasswordEncoder)
- `ModelMapper` ou `MapStruct` (para conversão DTO ↔ Entity)

## Métodos Públicos

### 1.1. registerUser(UserRegistrationDTO dto)

**Objetivo:** Criar novo usuário no sistema

**Parâmetros:**
- `UserRegistrationDTO`: contém name, email, password

**Retorno:** `UserResponseDTO`

**Fluxo de Execução:**

1. Validar se email já existe (usar repository.existsByEmail)
2. Se existir, lançar `EmailAlreadyExistsException`
3. Validar força da senha (mínimo 8 caracteres, complexidade)
4. Criptografar senha com `PasswordEncoder`
5. Criar entidade `User`
6. Definir campos automáticos (createdAt, active = true)
7. Salvar no banco via repository
8. Converter `User` para `UserResponseDTO`
9. Retornar DTO (sem senha)

**Validações:**

- Email único
- Email formato válido
- Senha mínimo 8 caracteres
- Nome mínimo 3 caracteres
- Não permitir espaços em branco no início/fim

**Exceções Lançadas:**

- `EmailAlreadyExistsException` - email duplicado
- `InvalidPasswordException` - senha fraca
- `ValidationException` - dados inválidos

---

### 1.2. getUserById(Long userId)

**Objetivo:** Buscar usuário por ID

**Retorno:** `UserResponseDTO`

**Fluxo:**

1. Buscar no repository por ID
2. Se não encontrar, lançar `UserNotFoundException`
3. Converter para DTO
4. Retornar

**Exceções:**

- `UserNotFoundException`

---

### 1.3. updateUser(Long userId, UserUpdateDTO dto)

**Objetivo:** Atualizar dados do usuário

**Fluxo:**

1. Buscar usuário existente
2. Se não encontrar, lançar exceção
3. Se email foi alterado, verificar se novo email já existe
4. Atualizar apenas campos permitidos (name, email)
5. Atualizar updatedAt
6. Salvar
7. Retornar DTO atualizado

**Regras:**

- Não permitir alterar ID
- Não permitir alterar senha por este método (ter método separado)
- Validar novo email se alterado

---

### 1.4. changePassword(Long userId, ChangePasswordDTO dto)

**Objetivo:** Alterar senha do usuário

**Parâmetros:**
- `ChangePasswordDTO`: currentPassword, newPassword, confirmPassword

**Fluxo:**

1. Buscar usuário
2. Verificar se senha atual está correta (passwordEncoder.matches)
3. Se incorreta, lançar `InvalidPasswordException`
4. Validar nova senha (força, diferente da atual)
5. Verificar se newPassword == confirmPassword
6. Criptografar nova senha
7. Atualizar
8. Salvar

**Validações:**

- Senha atual correta
- Nova senha forte
- Confirmação igual à nova senha
- Nova senha diferente da atual

---

### 1.5. deactivateUser(Long userId)

**Objetivo:** Desativar usuário (soft delete)

**Fluxo:**

1. Buscar usuário
2. Marcar active = false
3. Salvar
4. Retornar confirmação

**Regra Importante:**

- Não excluir fisicamente
- Manter todos os dados para auditoria