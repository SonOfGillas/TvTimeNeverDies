package com.example.tvtimeneverdie.di

import com.example.tvtimeneverdie.data.repository.AuthRepository
import com.example.tvtimeneverdie.data.repository.CommentsRepository
import com.example.tvtimeneverdie.data.repository.MovieRepository
import com.example.tvtimeneverdie.data.repository.TvShowRepository
import com.example.tvtimeneverdie.data.repository.UserMoviesRepository
import com.example.tvtimeneverdie.data.repository.UserShowsRepository

/**
 * Contenitore manuale delle dipendenze condivise: nessun framework di DI, solo singleton lazy.
 * Firebase (Auth/Firestore) si auto-inizializza sulla piattaforma Android tramite il
 * ContentProvider dell'SDK nativo (richiede solo google-services.json + plugin applicati).
 */
object AppContainer {
    val authRepository: AuthRepository by lazy { AuthRepository() }
    val tvShowRepository: TvShowRepository by lazy { TvShowRepository() }
    val userShowsRepository: UserShowsRepository by lazy { UserShowsRepository() }
    val commentsRepository: CommentsRepository by lazy { CommentsRepository() }
    val movieRepository: MovieRepository by lazy { MovieRepository() }
    val userMoviesRepository: UserMoviesRepository by lazy { UserMoviesRepository() }
}
