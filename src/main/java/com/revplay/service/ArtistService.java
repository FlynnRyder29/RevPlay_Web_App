package com.revplay.service;

import com.revplay.dto.ArtistProfileResponse;
import com.revplay.dto.ArtistRegisterRequest;

public interface ArtistService {

    ArtistProfileResponse registerArtist(ArtistRegisterRequest request);

    ArtistProfileResponse getMyProfile();

    ArtistProfileResponse updateProfile(ArtistRegisterRequest request);
}