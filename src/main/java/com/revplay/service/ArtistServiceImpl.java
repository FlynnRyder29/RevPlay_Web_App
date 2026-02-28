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

        if (artistRepository.existsByUser(user)) {
            throw new RuntimeException("Artist profile already exists");
        }

        Artist artist = new Artist();
        artist.setUser(user);
        artist.setArtistName(request.getArtistName());
        artist.setBio(request.getBio());
        artist.setGenre(request.getGenre());
        artist.setInstagram(request.getInstagram());
        artist.setTwitter(request.getTwitter());
        artist.setYoutube(request.getYoutube());
        artist.setSpotify(request.getSpotify());
        artist.setWebsite(request.getWebsite());

        artistRepository.save(artist);

        return mapToResponse(artist);
    }

    @Override
    public ArtistProfileResponse getMyProfile() {

        User user = getCurrentUser();

        Artist artist = artistRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Artist profile not found"));

        return mapToResponse(artist);
    }

    @Override
    public ArtistProfileResponse updateProfile(ArtistRegisterRequest request) {

        User user = getCurrentUser();

        Artist artist = artistRepository.findByUser(user)
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

        return mapToResponse(artist);
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private ArtistProfileResponse mapToResponse(Artist artist) {

        return ArtistProfileResponse.builder()
                .id(artist.getId())
                .username(artist.getUser().getUsername())
                .email(artist.getUser().getEmail())
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