package com.revplay.service;

import com.revplay.dto.ArtistProfileResponse;
import com.revplay.dto.ArtistRegisterRequest;
import com.revplay.dto.ArtistUpdateRequest;

public interface ArtistService {

    ArtistProfileResponse registerArtist(ArtistRegisterRequest request);

    ArtistProfileResponse getMyProfile();

    ArtistProfileResponse updateProfile(ArtistUpdateRequest request);
}