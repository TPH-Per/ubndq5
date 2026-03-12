-- V25: Seed required_documents and description for existing procedures

UPDATE procedure SET
    description = 'Thủ tục đăng ký khai sinh cho trẻ em mới sinh. Cần thực hiện trong vòng 60 ngày kể từ ngày sinh.',
    required_documents = 'Tờ khai đăng ký khai sinh (mẫu theo quy định)
Giấy chứng sinh do cơ sở y tế cấp (bản chính)
CCCD/CMND của cha hoặc mẹ (bản chính + 01 bản sao)
Sổ hộ khẩu hoặc giấy tờ chứng minh nơi cư trú (bản sao)
Giấy đăng ký kết hôn của cha, mẹ (nếu có, bản sao)'
WHERE procedure_code = 'TT001';

UPDATE procedure SET
    description = 'Thủ tục đăng ký kết hôn cho công dân tại địa phương. Hai bên nam nữ phải đủ điều kiện kết hôn theo quy định pháp luật.',
    required_documents = 'Tờ khai đăng ký kết hôn (mẫu theo quy định, 02 bản)
CCCD/CMND của cả hai bên (bản chính + 01 bản sao mỗi bên)
Giấy xác nhận tình trạng hôn nhân của mỗi bên (nơi thường trú cấp, còn hiệu lực trong 6 tháng)
Sổ hộ khẩu hoặc giấy tờ cư trú hợp lệ của cả hai bên (bản sao)'
WHERE procedure_code = 'TT002';

UPDATE procedure SET
    description = 'Xác nhận tình trạng hôn nhân để phục vụ các thủ tục hành chính khác. Giấy xác nhận có hiệu lực trong 06 tháng.',
    required_documents = 'CCCD/CMND (bản chính để đối chiếu)
Sổ hộ khẩu hoặc giấy tờ cư trú hợp lệ (bản sao)
Đơn đề nghị cấp giấy xác nhận tình trạng hôn nhân (mẫu theo quy định)'
WHERE procedure_code = 'TT003';

UPDATE procedure SET
    description = 'Đăng ký biến động đất đai khi có thay đổi về quyền sử dụng đất, quyền sở hữu tài sản gắn liền với đất.',
    required_documents = 'Đơn đăng ký biến động đất đai (mẫu 09/ĐK)
Giấy chứng nhận quyền sử dụng đất (Sổ đỏ/Sổ hồng, bản chính)
CCCD/CMND của người đứng tên (bản chính + 01 bản sao)
Hợp đồng mua bán/tặng cho/thừa kế có công chứng (bản chính + 01 bản sao)
Giấy tờ liên quan đến biến động (hợp đồng, bản án, quyết định...)
Sổ hộ khẩu (bản sao)'
WHERE procedure_code = 'TT004';

UPDATE procedure SET
    description = 'Cấp giấy phép kinh doanh cho hộ kinh doanh cá thể. Thời gian xử lý tối đa 10 ngày làm việc.',
    required_documents = 'Đơn đề nghị đăng ký hộ kinh doanh (mẫu theo quy định)
CCCD/CMND của chủ hộ kinh doanh (bản chính + 02 bản sao)
Sổ hộ khẩu hoặc giấy tờ chứng minh cư trú (bản sao)
Giấy tờ chứng minh quyền sử dụng địa điểm kinh doanh (hợp đồng thuê nhà hoặc giấy tờ nhà đất)
Ảnh 3x4 của chủ hộ kinh doanh (02 ảnh)'
WHERE procedure_code = 'TT005';
