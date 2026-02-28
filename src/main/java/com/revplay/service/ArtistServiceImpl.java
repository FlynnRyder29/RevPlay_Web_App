package com.revplay.service;

import com.revplay.dto.ArtistProfileResponse;
import com.revplay.dto.ArtistRegisterRequest;
import com.revplay.model.Artist;
import com.revplay.model.User;
import com.revplay.repository.ArtistRepository;
import com.revplay.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ArtistServiceImpl implements ArtistService {

    private final ArtistRepository artistRepository;
    private final UserRepository userRepository;

    @Override
    public ArtistProfileResponse registerArtist(ArtistRegisterRequest request) {

        User user = getCurrentUser();
        Long userId = user.getId();

        if (artistRepository.existsByUserId(userId)) {
            throw new RuntimeException("Artist profile already exists");
        }

        Artist artist = new Artist();
        artist.setUserId(userId);
        artist.setArtistName(request.getArtistName());
        artist.setBio(request.getBio());
        artist.setGenre(request.getGenre());
        artist.setInstagram(request.getInstagram());
        artist.setTwitter(request.getTwitter());
        artist.setYoutube(request.getYoutube());
        artist.setSpotify(request.getSpotify());
        artist.setWebsite(request.getWebsite());

        artistRepository.save(artist);

        return mapToResponse(artist, user);
    }

    @Override
    public ArtistProfileResponse getMyProfile() {

        User user = getCurrentUser();
        Long userId = user.getId();

        Artist artist = artistRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Artist profile not found"));

        return mapToResponse(artist, user);
    }

    @Override
    public ArtistProfileResponse updateProfile(ArtistRegisterRequest request) {

        User user = getCurrentUser();
        Long userId = user.getId();

        Artist artist = artistRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Artist profile not found"));

        artist.setArtistName(request.getArtistName());
        artist.setBio(request.getBio());
        artist.setGenre(request.getGenre());
        artist.setInstagram(request.getInstagram());
        artist.setTwitter(request.getTwitter());
        artist.setYoutube(request.getYoutube());
        artist.setSpotify(request.getSpotify());
        artist.setWebsite(request.getWebsite());

        artistRepository.save(artist);

        return mapToResponse(artist, user);
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private ArtistProfileResponse mapToResponse(Artist artist, User user) {

        return ArtistProfileResponse.builder()
                .id(artist.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .artistName(artist.getArtistName())
                .bio(artist.getBio())
                .genre(artist.getGenre())
                .profilePictureUrl(artist.getProfilePictureUrl())
                .bannerImageUrl(artist.getBannerImageUrl())
                .instagram(artist.getInstagram())
                .twitter(artist.getTwitter())
                .youtube(artist.getYoutube())
                .spotify(artist.getSpotify())
                .website(artist.getWebsite())
                .build();
    }
}