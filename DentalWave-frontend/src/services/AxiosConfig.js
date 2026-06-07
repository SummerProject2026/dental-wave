import axios from 'axios'
import { getToken } from './AuthService'

/**
 * Axios Request Interceptor
 *
 * This interceptor runs before every axios request.
 *
 * Its purpose is to automatically attach the JWT token
 * to all secured API requests after a user logs in.
 *
 * Without this interceptor, every service method would
 * need to manually add:
 *
 * headers: {
 *     Authorization: getToken()
 * }
 *
 * By configuring it here, all axios requests automatically
 * include the token if one exists.
 */
axios.interceptors.request.use(

    /**
     * Executes before the request is sent.
     *
     * @param config The axios request configuration
     * @returns Updated request configuration
     */
    function (config) {

        // Retrieve JWT token from local storage
        const token = getToken()

        // Only attach the Authorization header
        // if a token exists
        if (token) {
            config.headers['Authorization'] = token
        }

        return config
    },

    /**
     * Handles request configuration errors.
     *
     * @param error The request error
     * @returns Rejected promise
     */
    function (error) {
        return Promise.reject(error)
    }
)

/**
 * This file does not export anything.
 *
 * Import it once in main.jsx:
 *
 * import './services/AxiosConfig'
 *
 * Doing so registers the interceptor when
 * the application starts.
 */