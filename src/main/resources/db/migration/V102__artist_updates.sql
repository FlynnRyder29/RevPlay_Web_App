-- ============================================================
-- RevPlay Music Streaming Application
-- Flyway Migration: V100__artist_updates.sql
-- Description: Artist YouTube links, profile pictures, and name fixes
-- ============================================================

-- ============================================================
-- ARTIST YOUTUBE LINKS
-- ============================================================

UPDATE artists
SET youtube=
CASE
WHEN artist_name='Aria' THEN 'https://www.youtube.com/@AriaKpopFantastic'
WHEN artist_name='Marco Jazz' THEN 'https://www.youtube.com/@marcojazz4078'
WHEN artist_name='Boa' THEN 'https://www.youtube.com/@BoA'
WHEN artist_name='Djo' THEN 'https://www.youtube.com/@djomusic887'
WHEN artist_name='Richie Mitch & The Coal Miners' THEN 'https://www.youtube.com/@rmcmband'
WHEN artist_name='Surf Curse' THEN 'https://www.youtube.com/@surfcurse'
WHEN artist_name='Suki Waterhouse' THEN 'https://www.youtube.com/@SukiWaterhouse'
WHEN artist_name='The Smiths' THEN 'https://www.youtube.com/@thesmithsofficial'
WHEN artist_name='Walters' THEN 'https://www.youtube.com/@TheWaltersBand'
WHEN artist_name='MGMT' THEN 'https://www.youtube.com/channel/UCC2i4uIWWjx5L5V5Q5k7eiQ'
WHEN artist_name='TV Girl' THEN 'https://www.youtube.com/@teeveegirl'
WHEN artist_name='Ankit Tiwari' THEN 'https://www.youtube.com/@ankittiwarimusic'
WHEN artist_name='Faheem Abdullah' THEN 'https://www.youtube.com/channel/UCzRXuZ2TLCtVywE676SYIVg'
WHEN artist_name='Manan Bhardwaj' THEN 'https://www.youtube.com/results?search_query=manan+bhardwaj'
WHEN artist_name='Sukhwinder Singh' THEN 'https://www.youtube.com/channel/UCG9SWMK-e4q3b1oszt0Bpkw'
WHEN artist_name='Arijit Singh' THEN 'https://www.youtube.com/channel/UCtFOW7jJXChfFNoucRFqRmw'
WHEN artist_name='B Praak' THEN 'https://www.youtube.com/channel/UCXjFirfrQa_02dBnHshjbzA'
WHEN artist_name='Roop Kumar' THEN 'https://www.youtube.com/@roopkumarrathodofficial'
WHEN artist_name='Sonu Nigam' THEN 'https://www.youtube.com/channel/UCDYFISYJx2tSc6cyhvx0N5Q'
WHEN artist_name='Palak Muchhal' THEN 'https://www.youtube.com/@PalakMuchhalOfficial'
WHEN artist_name='Akon' THEN 'https://www.youtube.com/channel/UC6IBMCQ6-d7p41KHxOsq4RA'
WHEN artist_name='Dolly Parton' THEN 'https://www.youtube.com/channel/UCuGuRQHrvoNsD-AolV0X39g'
WHEN artist_name='Fiction Junction' THEN 'https://www.youtube.com/channel/UCiRiIYBxS1l0pN9f7AH_UeA'
WHEN artist_name='Lisa' THEN 'https://www.youtube.com/channel/UCqEfdEvLG5oQWNYlDQrGlKw'
WHEN artist_name='Bones UK' THEN 'https://www.youtube.com/@bonesukband'
WHEN artist_name='Man With a Mission and Milet' THEN 'https://www.youtube.com/channel/UCy21ToKvBD45wUPaSLkr5NQ'
WHEN artist_name='Poor Mans Poison' THEN 'https://www.youtube.com/channel/UC-RP9HS8bnLtV0E9B27M9dA'
WHEN artist_name='Serge Nova' THEN 'https://www.youtube.com/channel/UCGiNyvj-A0-xhVC4Tn68E9A'
WHEN artist_name='Against the Current' THEN 'https://www.youtube.com/channel/UCxMsgwldMZiuFTD6jjv32yQ'
WHEN artist_name='TheFatRat and Maisy Kay' THEN 'https://www.youtube.com/@TheFatRat'
WHEN artist_name='Yuiko Oraha' THEN 'https://www.youtube.com/@TheFatRat'
WHEN artist_name='Konomi Suzuki' THEN 'https://www.youtube.com/channel/UCuDILZhkm3kZHownsx_wH3A'
WHEN artist_name='Yoasobi' THEN 'https://www.youtube.com/channel/UCvpredjG93ifbCP1Y77JyFA'
WHEN artist_name='Gawr Gura' THEN 'https://www.youtube.com/@GawrGura'
WHEN artist_name='King Gnu' THEN 'https://www.youtube.com/channel/UCkB8HnJSDSJ2hkLQFUc-YrQ'
WHEN artist_name='Goose House' THEN 'https://www.youtube.com/channel/UCWzXfY6z7QGwzUadq3nXQmw'
WHEN artist_name='Gloria Gaynor' THEN 'https://www.youtube.com/channel/UCgVq3HlmkLoh9CFt9i7Syug'
WHEN artist_name='FictionJunction' THEN 'https://www.youtube.com/@FictionJunction_official'
WHEN artist_name='Lenny Code Fiction' THEN 'https://www.youtube.com/channel/UChz8gA2NREdH4tBmz3xgb7w'
WHEN artist_name='Fujii Kaze' THEN 'https://www.youtube.com/channel/UCNIy6zQyP7SuLEIaiwymfUA'
WHEN artist_name='Hiroyuki Sawano' THEN 'https://www.youtube.com/results?search_query=hiroyuki+sawano'
WHEN artist_name='Lisa feat. Felix of Stray Kids' THEN 'https://www.youtube.com/channel/UCqEfdEvLG5oQWNYlDQrGlKw'
WHEN artist_name='Ikimonogakari' THEN 'https://www.youtube.com/@ikimonogakari_official'
WHEN artist_name='SiM' THEN 'https://www.youtube.com/channel/UCcrvP_Z93Zts_Xd7UGKH2CQ'
WHEN artist_name='TK' THEN 'https://www.youtube.com/@BtkAnimeKTL30'
WHEN artist_name='Keane' THEN 'https://www.youtube.com/channel/UC_5iLk7KvfsHW4CIWFyzasg'
WHEN artist_name='Hyde' THEN 'https://www.youtube.com/@HYDEOfficial'
WHEN artist_name='Final Fantasy IX' THEN 'https://www.youtube.com/@FinalFantasyUnion'
WHEN artist_name='Miki Matsubara' THEN 'https://www.youtube.com/@staywithme_miki'
WHEN artist_name='Melanie Martinez' THEN 'https://www.youtube.com/@MelanieMartinez'
WHEN artist_name='Akari Kito' THEN 'https://www.youtube.com/@KitoAkari_Official'
WHEN artist_name='Akeboshi' THEN 'https://www.youtube.com/channel/UCDtFHFVClTaVgjkOzwcDL8A'
WHEN artist_name='Thousand Foot Krutch' THEN 'https://www.youtube.com/@tfkofficial'
ELSE youtube
END;

-- ============================================================
-- ARTIST PROFILE PICTURES
-- ============================================================

UPDATE artists
SET profile_picture_url =
    CASE
        WHEN artist_name='Billie Eilish' THEN 'https://upload.wikimedia.org/wikipedia/commons/c/c9/Billie_Eilish_at_Pukkelpop_Festival_-_18_AUGUST_2019_%2801%29_%28cropped%29.jpg'
        WHEN artist_name='Boa' THEN 'https://upload.wikimedia.org/wikipedia/commons/9/95/180417_%EB%B3%B4%EC%95%84_03_%28cropped%29_02.png'
        WHEN artist_name='Sukhwinder Singh' THEN 'https://i.scdn.co/image/ab6761610000e5ebc89e803f887e3829819bf82a'
        WHEN artist_name='Arijit Singh' THEN 'https://artistbookingcompany.com/wp-content/uploads/2024/03/arjit-singh-680x680.png'
        WHEN artist_name='Manan Bhardwaj' THEN 'https://cdn-images.dzcdn.net/images/artist/b0ba4389eb844f95e965aaa9ad6d9bda/1900x1900-000000-80-0-0.jpg'
        WHEN artist_name='B Praak' THEN 'https://www.mediamanthan.com/uploads/images/202508/image_1600x_68ad49e127508.webp'
        WHEN artist_name='Roop Kumar' THEN 'https://i.scdn.co/image/ab6761610000e5ebed9f71fa78741d6b30cb7d49'
        WHEN artist_name='Sonu Nigam' THEN 'https://artistbookingcompany.com/wp-content/uploads/2024/03/sonu-nigam-680x680.jpg'
        WHEN artist_name='Palak Mucchal' THEN 'https://i.scdn.co/image/ab6761610000e5eb0608ee2e47f6f470211831b9'
        WHEN artist_name='Ed Sheeran' THEN 'https://upload.wikimedia.org/wikipedia/commons/c/c1/Ed_Sheeran-6886_%28cropped%29.jpg'
        WHEN artist_name='Justin Bieber' THEN 'https://m.media-amazon.com/images/M/MV5BMjE1NjMxMDUyM15BMl5BanBnXkFtZTgwODMzNDM1NTE@._V1_.jpg'
        WHEN artist_name='Akon' THEN 'https://upload.wikimedia.org/wikipedia/commons/8/80/Akon_DF2_4639_%2847859034612%29_%28cropped%29.jpg'
        WHEN artist_name='Dolly Parton' THEN 'https://cdn.britannica.com/73/280373-050-527E607A/Dolly-Parton-1995.jpg'
        WHEN artist_name='Enrique Iglesias' THEN 'https://image.tmdb.org/t/p/w500/hz21f8xF6l5PLYW0MuKe8RTyRzA.jpg'
        WHEN artist_name='Linkin Park' THEN 'https://i.guim.co.uk/img/media/ed3fde26a6a3b03eb43f2e9b5179d189e193a4c2/0_2259_4480_3584/master/4480.jpg?width=465&dpr=1&s=none&crop=none'
        WHEN artist_name='FictionJunction' THEN 'https://canta-per-me.net/images/vocalists/more/yuki-and-fj.jpg'
        WHEN artist_name='Fiction Junction' THEN 'https://canta-per-me.net/images/vocalists/more/yuki-and-fj.jpg'
        WHEN artist_name='Marco Jazz' THEN 'https://i0.wp.com/www.marcomarconi.com/wp-content/uploads/2022/09/Marco-Marconi-Hussigny-Hi-Res-scaled.jpg?fit=2560%2C1708'
        WHEN artist_name='Mac DeMarco' THEN 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTB_rWM0z73v_veeUXc5lrXfFKpd3GbZNYlBA&s'
        WHEN artist_name='Richy Mitch & The Coal Miners' THEN 'https://yt3.googleusercontent.com/JNwj_hnQ5pu8ovycXF68eahHYfq8RhNBeLeNPFNWlL0fr1SHRCxwJMsK_cczmInbjlecF-p4ACM=s900-c-k-c0x00ffffff-no-rj'
        WHEN artist_name='RadioHead' THEN 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSv9-WxCm-N_ExIv02HvFHXcarYGeyLFVBzbA&s'
        WHEN artist_name='Surf Curse' THEN 'https://i.scdn.co/image/ab6761610000e5eb7fabcff4fe07270f07039355'
        WHEN artist_name='Laufey' THEN 'https://www.ozarksfirst.com/wp-content/uploads/sites/65/2025/08/68a4e01459f009.79709182.jpeg?w=1440&h=2560&crop=1'
        WHEN artist_name='Suki Waterhouse' THEN 'https://m.media-amazon.com/images/M/MV5BMzIzMTFlZGUtMmNlYy00YWY3LTkyNGUtNzAxMGFmYmMwOGE0XkEyXkFqcGc@._V1_.jpg'
        WHEN artist_name='Imogen Heap' THEN 'https://upload.wikimedia.org/wikipedia/commons/f/ff/Imogen_Heap%3B_Press_conferences_stage%3B_MEO_Arena%3B_Web_Summit_2024_%28cropped_portrait%29.jpg'
        WHEN artist_name='The Smiths' THEN 'https://upload.wikimedia.org/wikipedia/commons/thumb/6/68/The_Smiths_%281984_Sire_publicity_photo%29_002.jpg/330px-The_Smiths_%281984_Sire_publicity_photo%29_002.jpg'
        WHEN artist_name='Walters' THEN 'https://i.scdn.co/image/ab6761610000e5ebb63c6c447c9c484c6e87d509'
        WHEN artist_name='Tame Impala' THEN 'https://www.rollingstone.com/wp-content/uploads/2019/05/tame-impala-lead-photo.jpg'
        WHEN artist_name='Tyler the Creator' THEN 'https://upload.wikimedia.org/wikipedia/commons/thumb/7/71/Tyler_the_Creator_%2852163761341%29_%28cropped%29.jpg/960px-Tyler_the_Creator_%2852163761341%29_%28cropped%29.jpg'
        WHEN artist_name='MGMT' THEN 'https://i.guim.co.uk/img/static/sys-images/Guardian/Pix/pictures/2010/4/7/1270655371664/mgmt-Andrew-VanWyngarden--001.jpg?width=465&dpr=1&s=none&crop=none'
        WHEN artist_name='TV Girl' THEN 'https://cdn-images.dzcdn.net/images/artist/9cec5aa40642063a408ba52be1310b20/1900x1900-000000-80-0-0.jpg'
        WHEN artist_name='Faheem Ashraf' THEN 'https://www.hollywoodreporterindia.com/_next/image?url=https%3A%2F%2Fcdn.hollywoodreporterindia.com%2Farticle%2F2025-12-22T06%253A45%253A45.232Z-THR%2520M%2520209-fotor-20251222121233.jpg&w=3840&q=75'
        WHEN artist_name='Selena Gomez' THEN 'https://s.yimg.com/ny/api/res/1.2/YsBUMIl6GVo5qNiTiuiG.A--/YXBwaWQ9aGlnaGxhbmRlcjt3PTEyNDI7aD0xODYzO2NmPXdlYnA-/https://media.zenfs.com/en/us_magazine_896/0636653f3654741de5c490dba6e1e4c6'
        WHEN artist_name='D4vd' THEN 'https://picsum.photos/seed/d4vd-pfp/300/300'
        WHEN artist_name='LiSA' THEN 'https://upload.wikimedia.org/wikipedia/commons/d/df/LiSA_by_Gage_Skidmore.jpg'
        WHEN artist_name='Ark Petrol' THEN 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcST0THwbcqFrpBAbvzm5o5LPhgAQF4wdyGojjZBGU7DQwTYS7ujuk82mpXEtP-xSUFIx_mwtG8_c6wRmDMu74hPVmIWFQcrAViTmTRpOqW4hA&s=10'
        WHEN artist_name='Bones UK' THEN 'https://upload.wikimedia.org/wikipedia/commons/0/0c/Bones_UK_10.21.2019_-_50425293592.jpg'
        WHEN artist_name='Man With a Mission and Milet' THEN 'https://i.namu.wiki/i/tP96gmHmwfZ7AQC0QR6RsTpII0JQMEkLC9ZghQAueEgeV0rAkbf4579D0o0QtYE0mWk7F3BWrg3U3KWwAuVkiQ.webp'
        WHEN artist_name='Poor Mans Poison' THEN 'https://i1.sndcdn.com/avatars-000554628783-iz0c6p-t1080x1080.jpg'
        WHEN artist_name='Serge Nova' THEN 'https://f4.bcbits.com/img/0037341807_10.jpg'
        WHEN artist_name='Against the Current' THEN 'https://www.musicscenemedia.com/content/images/2023/05/307038605_5681285351892210_4447132860655277542_n.png'
        WHEN artist_name='TheFatRat and Maisy Kay' THEN 'https://www.billboard.com/wp-content/uploads/media/The-Fat-Rat-2016-press-billboard-1548.jpg'
        WHEN artist_name='Yuiko Ohara' THEN 'https://yt3.googleusercontent.com/ytc/AIdro_kVGy58wfAaPOEejZG-LXmfu4Imyihlb6lcJneBM2iPGaE=s900-c-k-c0x00ffffff-no-rj'
        WHEN artist_name='Konomi Suzuki' THEN 'https://i.scdn.co/image/ab6761610000e5ebce9e8797e4189b7c277ead8f'
        WHEN artist_name='YOASOBI' THEN 'https://charts-static.billboard.com/img/2020/03/yoasobi-0gc-344x344.jpg'
        WHEN artist_name='Gawr Gura' THEN 'https://images.genius.com/646b3563618d53b43a7336c7059eee70.1000x1000x1.png'
        WHEN artist_name='King Gnu' THEN 'https://cdn-images.dzcdn.net/images/artist/eab5445c9e15a77ed8a92fac13546d86/1900x1900-000000-80-0-0.jpg'
        WHEN artist_name='Goose House' THEN 'https://i.scdn.co/image/ab67616d0000b2735ecb196621dabbbd2a347063'
        WHEN artist_name='Gloria Gaynor' THEN 'https://yt3.googleusercontent.com/m16xTJie4MZjenCJ-hskr2erWiGbj4oQS0Ww6tqflyi1mk7C9Bj9A05VBQBRJirsRLMqNEQMHg=s900-c-k-c0x00ffffff-no-rj'
        WHEN artist_name='Field of View' THEN 'https://lastfm.freetls.fastly.net/i/u/ar0/26ee90039fc748f0b41cc362ef42c51f.jpg'
        WHEN artist_name='Lenny Code Fiction' THEN 'https://is1-ssl.mzstatic.com/image/thumb/Features221/v4/d7/98/5d/d7985ddb-c731-4e4e-2967-c7d98b3da3ac/mzl.ovtcuikm.jpg/800x800vb.webp'
        WHEN artist_name='Fujii Kaze' THEN 'https://upload.wikimedia.org/wikipedia/commons/4/49/Fujii_Kaze_performing_during_Best_Of_Fujii_Kaze_2020-2024_Asia_Tour_in_Axiata_Arena_Kuala_Lumpur_%28cropped%29.jpg'
        WHEN artist_name='Hiroyuki Sawano' THEN 'https://cdn-images.dzcdn.net/images/artist/e00822bee264f4888fb7e069eaa45967/1900x1900-000000-80-0-0.jpg'
        WHEN artist_name='Ikimonogakari' THEN 'https://www.billboard.com/wp-content/uploads/2023/11/Ikimonogakari-cr-Ayaka-Horiuchi-bb-japan-billboard-1548.jpg'
        WHEN artist_name='SiM' THEN 'https://jrocknews.com/wp-content/uploads/2022/01/sim-the-rumbling-group-2022-1170x781.jpg'
        WHEN artist_name='TK' THEN 'https://cdn.tatlerasia.com/tatlerasia/i/2022/04/08140212-tk-2_cover_1500x1200.jpg'
        WHEN artist_name='Alan Walker' THEN 'https://i0.wp.com/celebmix.com/wp-content/uploads/2023/04/alan-walker-takes-it-back-to-the-beginning-with-his-new-single-dreamer-which-pays-homage-to-his-breakthrough-hits-faded-and-alone-01-scaled.jpg?fit=2560%2C1920&ssl=1'
        WHEN artist_name='Imagine Dragons' THEN 'https://www.coca-cola.com/content/dam/onexp/in/en/offerings/coke-studio/artists/2_Imagine_Dragons_by_Eric_Ray_Davidson_GREEN_04_1-1.jpg'
        WHEN artist_name='League of Legends' THEN 'https://i.ytimg.com/vi/g9GI7XRRQMQ/mqdefault.jpg'
        WHEN artist_name='Keane' THEN 'https://cdn-images.dzcdn.net/images/artist/213ee8aa14bc7b9ac86cebfadd2a6ee1/1900x1900-000000-80-0-0.jpg'
        WHEN artist_name='Hyde' THEN 'https://a.storyblok.com/f/178900/735x735/22cdc0d7a5/mugen-cd-jacket.jpg/m/filters:quality(95)format(webp)'
        WHEN artist_name='Final Fantasy IX' THEN 'https://cache-eu.finalfantasy.com/assets/web/title/logo_ff9_en-5ee9d35596777caa408169470637e6f622f703329d95a194f6f548c0d704e2cd.png'
        WHEN artist_name='Miki Matsubara' THEN 'https://ponycanyon.us/wp-content/uploads/2024/02/%E6%9D%BE%E5%8E%9F%E3%81%BF%E3%81%8Dap_%E6%AD%A3%E6%96%B9%E5%BD%A2-scaled.jpg'
        WHEN artist_name='Melanie Martinez' THEN 'https://cdn.abcotvs.com/dip/images/5696973_111419-kgo-ap-melanie-martinez-img.jpg'
        WHEN artist_name='Akari Kito' THEN 'https://myanimelist.net/images/voiceactors/2/70148.jpg'
        WHEN artist_name='Akeboshi' THEN 'https://r2.theaudiodb.com/images/media/artist/thumb/uprryq1358669083.jpg'
        WHEN artist_name='Thousand Foot Krutch' THEN 'https://cdn-images.dzcdn.net/images/artist/0d014deeae08ebbc2849041e8c844563/1900x1900-000000-80-0-0.jpg'
        ELSE profile_picture_url
    END;

-- ============================================================
-- ARTIST NAME & PICTURE FIXES
-- ============================================================

UPDATE artists SET artist_name='Faheem Abdullah' WHERE id=22;
UPDATE artists SET profile_picture_url='https://images.squarespace-cdn.com/content/v1/5b0dd7581aef1d319395b854/1743708875326-Z71EX28SMZEGH1I4A6PR/unnamed+%2863%29.jpg' WHERE artist_name='Djo';

-- ============================================================
-- ARTIST WEBSITE UPDATES
-- ============================================================

UPDATE artists
SET website = CASE
    WHEN id = 69 THEN 'https://www.melaniemartinezmusic.com/'
    WHEN id = 7  THEN 'https://www.boaukofficial.com/tour'
END
WHERE id IN (69, 7);

-- ============================================================
-- ARTIST INSTAGRAM UPDATES
-- ============================================================

UPDATE artists
SET instagram = CASE
    WHEN id = 31 THEN 'https://www.instagram.com/lilbieber/'
    WHEN id = 22 THEN 'https://www.instagram.com/faheemabdullahworld/'
    WHEN id = 27 THEN 'https://www.instagram.com/roopkumarrathod.official/'
    WHEN id = 23 THEN 'https://www.instagram.com/manan_bhardwaj_official/'
    WHEN id = 24 THEN 'https://www.instagram.com/sukhwindersinghofficial/'
    WHEN id = 40 THEN 'https://www.instagram.com/bonesbandbones/'
    WHEN id = 43 THEN 'https://www.instagram.com/serge.nova.music/reels/'
    WHEN id = 46 THEN 'https://www.instagram.com/ohara_yuiko/'
    WHEN id = 72 THEN 'https://www.instagram.com/officialtfk/'
    WHEN id = 17 THEN 'https://www.instagram.com/tameimpala/'
    WHEN id = 25 THEN 'https://www.instagram.com/arijitsingh/'
    WHEN id = 32 THEN 'https://www.instagram.com/akon/'
    WHEN id = 35 THEN 'https://www.instagram.com/selenagomez/'
    WHEN id = 34 THEN 'https://www.instagram.com/enriqueiglesias/'
    WHEN id = 36 THEN 'https://www.instagram.com/linkinpark/'
END
WHERE id IN (31, 22, 27, 23, 24, 40, 43, 46, 72, 17, 25, 32, 35, 34, 36);
