-- ========================================================
-- DOPAMINE SERVERSIDE SUPABASE DATABASE SCHEMA & MIGRATION
-- ========================================================

-- 1. Kullanıcılar Tablosu (Users Table with Real Passwords & Roles)
CREATE TABLE IF NOT EXISTS public.users (
    id TEXT PRIMARY KEY,
    username TEXT UNIQUE NOT NULL,
    password TEXT NOT NULL,
    full_name TEXT NOT NULL,
    is_moderator BOOLEAN DEFAULT FALSE,
    last_nudge_timestamp BIGINT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL
);

-- 2. Haftalık Saha Raporları Tablosu (Reports Table with On-Time Tracking)
CREATE TABLE IF NOT EXISTS public.reports (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL,
    username TEXT NOT NULL,
    user_full_name TEXT NOT NULL,
    new_members_count INT DEFAULT 0,
    home_visits_count INT DEFAULT 0,
    shop_visits_count INT DEFAULT 0,
    book_gifts_count INT DEFAULT 0,
    brochure_distribution_count INT DEFAULT 0,
    sticker_pasting_count INT DEFAULT 0,
    logo_gifts_count INT DEFAULT 0,
    field_work_participants TEXT,
    submission_timestamp BIGINT NOT NULL,
    is_submitted_on_time BOOLEAN DEFAULT TRUE,
    status TEXT DEFAULT 'PENDING',
    rejection_reason TEXT,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL
);

-- 3. Şifre Sıfırlama Talepleri Tablosu (Password Reset Requests Table)
CREATE TABLE IF NOT EXISTS public.password_resets (
    id TEXT PRIMARY KEY,
    username TEXT NOT NULL,
    message TEXT NOT NULL,
    status TEXT DEFAULT 'PENDING',
    timestamp BIGINT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL
);

-- RLS (Row Level Security) Ayarları - Anonim Erişim İzinleri
ALTER TABLE public.users ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.reports ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.password_resets ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Allow public select users" ON public.users;
DROP POLICY IF EXISTS "Allow public insert users" ON public.users;
DROP POLICY IF EXISTS "Allow public update users" ON public.users;
DROP POLICY IF EXISTS "Allow public delete users" ON public.users;

CREATE POLICY "Allow public select users" ON public.users FOR SELECT USING (true);
CREATE POLICY "Allow public insert users" ON public.users FOR INSERT WITH CHECK (true);
CREATE POLICY "Allow public update users" ON public.users FOR UPDATE USING (true);
CREATE POLICY "Allow public delete users" ON public.users FOR DELETE USING (true);

DROP POLICY IF EXISTS "Allow public select reports" ON public.reports;
DROP POLICY IF EXISTS "Allow public insert reports" ON public.reports;
DROP POLICY IF EXISTS "Allow public update reports" ON public.reports;

CREATE POLICY "Allow public select reports" ON public.reports FOR SELECT USING (true);
CREATE POLICY "Allow public insert reports" ON public.reports FOR INSERT WITH CHECK (true);
CREATE POLICY "Allow public update reports" ON public.reports FOR UPDATE USING (true);

DROP POLICY IF EXISTS "Allow public select resets" ON public.password_resets;
DROP POLICY IF EXISTS "Allow public insert resets" ON public.password_resets;
DROP POLICY IF EXISTS "Allow public update resets" ON public.password_resets;

CREATE POLICY "Allow public select resets" ON public.password_resets FOR SELECT USING (true);
CREATE POLICY "Allow public insert resets" ON public.password_resets FOR INSERT WITH CHECK (true);
CREATE POLICY "Allow public update resets" ON public.password_resets FOR UPDATE USING (true);

-- 4. Varsayılan Moderatör ve Test Kullanıcılarını Ekleme
INSERT INTO public.users (id, username, password, full_name, is_moderator)
VALUES 
    ('user_mod_1', 'mod', '1234', 'Sistem Moderatörü', TRUE),
    ('user_ahmet_1', 'ahmet', '1234', 'Ahmet Yılmaz', FALSE),
    ('user_mehmet_1', 'mehmet', '1234', 'Mehmet Kaya', FALSE),
    ('user_ayse_1', 'ayse', '1234', 'Ayşe Demir', FALSE)
ON CONFLICT (username) DO NOTHING;
