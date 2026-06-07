package com.summerproject2026.DentalWave.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response returned after a successful authentication request.
 *
 * Contains the JWT access token and basic information about
 * the authenticated user.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JwtAuthResponse {

    /** JWT access token used to authenticate future requests. */
    private String accessToken;

    /** Type of authentication token. */
    private String tokenType = "Bearer";

    /** Role assigned to the authenticated user. */
    private String role;

    /** Username of the authenticated user. */
    private String username;

    /** Email address of the authenticated user. */
    private String email;

    /** User Id which  will be used to identify who have  access to  what page*/
    private   Long id;

}