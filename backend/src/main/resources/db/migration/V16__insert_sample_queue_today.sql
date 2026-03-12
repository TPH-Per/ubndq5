-- =============================================
-- V16: Insert Sample Queue Data for Today
-- Thêm 10 lượt hẹn trong hàng chờ cho ngày hôm nay
-- (Cập nhật dùng citizen inline fields thay vì bảng Citizen)
-- =============================================

DO $$
DECLARE 
    v_procedure_id INTEGER;
    v_counter_id INTEGER;
    v_today DATE := CURRENT_DATE;
    v_cccd VARCHAR(12);
    v_name VARCHAR(100);
    v_phone VARCHAR(20);
    v_app_code VARCHAR(50);
    v_queue_num INTEGER;
    v_expected_time TIME;
    v_app_id INTEGER;
    
    -- Danh sách tên mẫu
    v_names TEXT[] := ARRAY[
        'Nguyễn Văn An', 'Trần Thị Bình', 'Lê Hoàng Cường', 'Phạm Thị Dung', 'Hoàng Văn Em',
        'Đỗ Thị Phương', 'Vũ Minh Giang', 'Bùi Thanh Hà', 'Ngô Văn Inh', 'Đinh Thị Kim'
    ];
BEGIN 
    -- Lấy procedure đầu tiên và counter đầu tiên
    SELECT id INTO v_procedure_id FROM procedure WHERE is_active = true LIMIT 1;
    SELECT id INTO v_counter_id FROM counter WHERE is_active = true LIMIT 1;

    -- Nếu không có procedure hoặc counter, thoát
    IF v_procedure_id IS NULL OR v_counter_id IS NULL THEN 
        RAISE NOTICE 'No active procedure or counter found, skipping sample data.';
        RETURN;
    END IF;

    -- Tạo 10 lượt hẹn
    FOR i IN 1..10 LOOP 
        v_cccd := '0123456789' || LPAD(i::TEXT, 2, '0');
        v_name := v_names[i];
        v_phone := '0901000' || LPAD(i::TEXT, 3, '0');
        v_app_code := 'Q' || TO_CHAR(v_today, 'YYYYMMDD') || LPAD(i::TEXT, 3, '0');
        v_queue_num := i;
        v_expected_time := ('08:00'::TIME + (i * INTERVAL '15 minutes'))::TIME;

        -- Kiểm tra xem application đã tồn tại chưa
        IF NOT EXISTS (SELECT 1 FROM application WHERE application_code = v_app_code) THEN 
            -- Insert application với thông tin citizen lưu TRỰC TIẾP (inline)
            INSERT INTO application (
                application_code,
                citizen_cccd,
                citizen_name,
                citizen_phone,
                citizen_email,
                procedure_id,
                current_phase,
                queue_number,
                queue_prefix,
                priority,
                deadline,
                created_at
            )
            VALUES (
                v_app_code,
                v_cccd,
                v_name,
                v_phone,
                LOWER(REPLACE(v_name, ' ', '.')) || '@example.com',
                v_procedure_id,
                1, -- PHASE 1: CHỜ GỌI
                v_queue_num,
                'A',
                CASE WHEN i = 5 THEN 1 ELSE 0 END, -- Người thứ 5 ưu tiên
                v_today + INTERVAL '3 days',
                NOW()
            )
            RETURNING id INTO v_app_id;

            -- Insert vào application_history
            INSERT INTO application_history (
                application_id,
                counter_id,
                phase_from,
                phase_to,
                action,
                content,
                appointment_date,
                expected_time,
                created_at
            )
            VALUES (
                v_app_id,
                v_counter_id,
                0,
                1,
                'CREATE',
                'Đặt lịch hẹn qua Zalo (Dữ liệu mẫu)',
                v_today,
                v_expected_time,
                NOW()
            );

            RAISE NOTICE 'Created queue A% for citizen % (%)', v_queue_num, v_cccd, v_name;
        END IF;
    END LOOP;
END $$;