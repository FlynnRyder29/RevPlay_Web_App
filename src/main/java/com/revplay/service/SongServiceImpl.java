package com.revplay.service;

import com.revplay.dto.SongCreateRequest;
import com.revplay.dto.SongResponse;
import com.revplay.dto.SongUpdateRequest;
import com.revplay.exception.ResourceNotFoundException;
import com.revplay.model.Album;
import com.revplay.model.Artist;
import com.revplay.model.Song;
import com.revplay.model.User;
import com.revplay.repository.AlbumRepository;
import com.revplay.repository.ArtistRepository;
import com.revplay.repository.SongRepository;
import com.revplay.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SongServiceImpl implements SongService {

    private final SongRepository songRepository;
    private final ArtistRepository artistRepository;
    private final UserRepository userRepository;
    private final AlbumRepository albumRepository;

    @Override
    public SongResponse createSong(SongCreateRequest request) {

        Artist artist = getCurrentArtist();

        Song song = new Song();
        song.setTitle(request.getTitle());
        song.setGenre(request.getGenre());
        song.setDuration(request.getDuration());
        song.setAudioUrl(request.getAudioUrl());
        song.setCoverImageUrl(request.getCoverImageUrl());
        song.setReleaseDate(request.getReleaseDate());

        // Default visibility if null
        song.setVisibility(
                request.getVisibility() != null
                        ? request.getVisibility()
                        : Song.Visibility.PUBLIC
        );

        song.setArtist(artist);

        if (request.getAlbumId() != null) {
            Album album = albumRepository.findById(request.getAlbumId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Album", "id", request.getAlbumId()
                    ));
            song.setAlbum(album);
        }

        songRepository.save(song);

        return mapToResponse(song);
    }

    @Override
    public SongResponse updateSong(Long songId, SongUpdateRequest request) {

        Song song = getOwnedSong(songId);

        if (request.getTitle() != null)
            song.setTitle(request.getTitle());

        if (request.getGenre() != null)
            song.setGenre(request.getGenre());

        if (request.getDuration() != null)
            song.setDuration(request.getDuration());

        if (request.getAudioUrl() != null)
            song.setAudioUrl(request.getAudioUrl());

        if (request.getCoverImageUrl() != null)
            song.setCoverImageUrl(request.getCoverImageUrl());

        if (request.getReleaseDate() != null)
            song.setReleaseDate(request.getReleaseDate());

        if (request.getVisibility() != null)
            song.setVisibility(request.getVisibility());

        if (request.getAlbumId() != null) {
            Album album = albumRepository.findById(request.getAlbumId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Album", "id", request.getAlbumId()
                    ));
            song.setAlbum(album);
        }

        songRepository.save(song);

        return mapToResponse(song);
    }

    @Override
    public void deleteSong(Long songId) {

        Song song = getOwnedSong(songId);

        songRepository.delete(song);
    }

    @Override
    public SongResponse toggleVisibility(Long songId) {

        Song song = getOwnedSong(songId);

        if (song.getVisibility() == Song.Visibility.PUBLIC) {
            song.setVisibility(Song.Visibility.PRIVATE);
        } else {
            song.setVisibility(Song.Visibility.PUBLIC);
        }

        songRepository.save(song);

        return mapToResponse(song);
    }

    private Artist getCurrentArtist() {

        String username = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", "username", username
                ));

        return artistRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Artist", "userId", user.getId()
                ));
    }

    private Song getOwnedSong(Long songId) {

        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Song", "id", songId
                ));

        Artist currentArtist = getCurrentArtist();

        if (!song.getArtist().getId().equals(currentArtist.getId())) {
            throw new RuntimeException("You do not own this song.");
        }

        return song;
    }

    private SongResponse mapToResponse(Song song) {
        return SongResponse.builder()
                .id(song.getId())
                .title(song.getTitle())
                .genre(song.getGenre())
                .duration(song.getDuration())
                .audioUrl(song.getAudioUrl())
                .coverImageUrl(song.getCoverImageUrl())
                .releaseDate(song.getReleaseDate())
                .playCount(song.getPlayCount())
                .visibility(song.getVisibility())
                .artistName(song.getArtist().getArtistName())
                .createdAt(song.getCreatedAt())
                .build();
    }
}