-- V28: Make title and content optional for citizen feedbacks
ALTER TABLE "GopYPhanAnh"
ALTER COLUMN tieude DROP NOT NULL;
ALTER TABLE "GopYPhanAnh"
ALTER COLUMN noidung DROP NOT NULL;