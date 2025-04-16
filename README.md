# 🔐 Hệ thống xác thực và phân quyền - Spring Boot

## ✅ Tính năng hiện có

- **JPA (Spring Data JPA)**  
  Truy xuất và thao tác dữ liệu từ cơ sở dữ liệu một cách tiện lợi, sử dụng Repository interface.

- **Spring Security**  
  Bảo mật ứng dụng, kiểm soát truy cập theo vai trò (Role-based Access Control).

- **OAuth2 Client & Resource Server**  
  - Cho phép đăng nhập bằng bên thứ ba như Google, Facebook,...
  - Hỗ trợ xác thực người dùng thông qua token OAuth2 (Bearer Token).

- **Validation**  
  Kiểm tra và xác thực dữ liệu đầu vào (form, API request) bằng annotation như `@Valid`, `@NotBlank`,...

- **Redis**  
  Sử dụng Redis để lưu **danh sách token bị thu hồi (blacklist)** sau khi người dùng **logout**.  
  Khi một request kèm token đến, hệ thống sẽ kiểm tra xem token đó có nằm trong blacklist Redis không, nếu có thì từ chối truy cập.

- **Spring Mail**  
  Tích hợp gửi email:
  - Kích hoạt tài khoản
  - Đặt lại mật khẩu
  - Gửi thông báo hệ thống,...

- **Lombok**  
  Sử dụng để giảm code boilerplate như getter/setter, constructor, builder,...

- **MySQL**  
  Cơ sở dữ liệu chính dùng để lưu trữ thông tin người dùng, phân quyền, token,...

---

## ⚙️ Công nghệ sử dụng

| Công nghệ             | Phiên bản         |
|----------------------|-------------------|
| Java                 | 17                |
| Spring Boot          | 3.4.4             |
| Spring Security      | Mặc định theo Spring Boot |
| MySQL                | 8+                |
| Redis                | 6+                |
| OAuth2               | Spring Security OAuth2 Client & Resource Server |
| Maven                | Dùng để quản lý dependency |
| Lombok               | Dùng annotation hỗ trợ code |

---

## 📫 Liên hệ

**Nguyễn Sao**  
📧 Email: nguyensaovn2019@gmail.com
📱 SĐT: 039 244 5255
📍 Github: [github.com/nguyensao](https://github.com/nguyensao)

---

> Cảm ơn bạn đã đọc README này. Nếu có góp ý hoặc vấn đề gì liên quan đến dự án, vui lòng liên hệ qua thông tin bên trên!
