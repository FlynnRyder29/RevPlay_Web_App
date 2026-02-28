package com.revplay.service;

import com.revplay.dto.ArtistProfileResponse;
import com.revplay.dto.ArtistRegisterRequest;
import com.revplay.dto.ArtistUpdateRequest;
import com.revplay.exception.DuplicateResourceException;
import com.revplay.exception.ResourceNotFoundException;
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

        if (artistRepository.existsByUserId(user.getId())) {
            throw new DuplicateResourceException(
                    "Artist profile already exists for this user"
            );
        }

        Artist artist = new Artist();
        artist.setUserId(user.getId());
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

        Artist artist = artistRepository
                .findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Artist",
                        "userId",
                        user.getId()
                ));

        return mapToResponse(artist);
    }

    @Override
    public ArtistProfileResponse updateProfile(ArtistUpdateRequest request) {

        User user = getCurrentUser();

        Artist artist = artistRepository
                .findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Artist",
                        "userId",
                        user.getId()
                ));

        // Prevent null overwrite (BLOCKING FIX)
        if (request.getArtistName() != null)
            artist.setArtistName(request.getArtistName());

        if (request.getBio() != null)
            artist.setBio(request.getBio());

        if (request.getGenre() != null)
            artist.setGenre(request.getGenre());

        if (request.getInstagram() != null)
            artist.setInstagram(request.getInstagram());

        if (request.getTwitter() != null)
            artist.setTwitter(request.getTwitter());

        if (request.getYoutube() != null)
            artist.setYoutube(request.getYoutube());

        if (request.getSpotify() != null)
            artist.setSpotify(request.getSpotify());

        if (request.getWebsite() != null)
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
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User",
                        "username",
                        username
                ));
    }

    private ArtistProfileResponse mapToResponse(Artist artist) {
        return ArtistProfileResponse.builder()
                .id(artist.getId())
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