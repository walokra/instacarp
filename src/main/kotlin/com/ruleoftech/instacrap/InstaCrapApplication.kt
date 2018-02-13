package com.ruleoftech.instacrap

import me.postaddict.instagram.scraper.Instagram
import me.postaddict.instagram.scraper.cookie.CookieHashSet
import me.postaddict.instagram.scraper.cookie.DefaultCookieJar
import me.postaddict.instagram.scraper.interceptor.ErrorInterceptor
import me.postaddict.instagram.scraper.model.Account
import me.postaddict.instagram.scraper.model.Comment
import me.postaddict.instagram.scraper.model.Media
import me.postaddict.instagram.scraper.model.PageObject
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

fun main(args: Array<String>) {

    val loggingInterceptor = HttpLoggingInterceptor()
    loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

    // Show requests and errors, store cookies
    val httpClient = OkHttpClient.Builder()
        .addNetworkInterceptor(loggingInterceptor)
        .addInterceptor(ErrorInterceptor())
        .cookieJar(DefaultCookieJar(CookieHashSet()))
        .build()

    val client = Instagram(httpClient)

    // Top post by tag
    val tag = client.getTagByName("unelmiakohti")
    val topPostWithCommentsOrNot: Media = tag.mediaRating.topPosts.firstOrNull { it.commentCount > 0 }
        ?: tag.mediaRating.topPosts[0]

    println("### \n" +
        "Caption: ${topPostWithCommentsOrNot.caption} \n" +
        "Display URL: ${topPostWithCommentsOrNot.displayUrl}\n" +
        "Comment count: ${topPostWithCommentsOrNot.commentCount}\n" +
        "Like count: ${topPostWithCommentsOrNot.likeCount}\n" +
        "User: ${topPostWithCommentsOrNot.owner}\n" +
        "###")

    // User's latest media
    val account: Account = client.getAccountById(topPostWithCommentsOrNot.owner.id)
    val userMedias: PageObject<Media> = client.getMedias(account.username, 1)
    val latestMediaWithComments = userMedias.nodes.firstOrNull { it.commentCount > 0 } ?: userMedias.nodes[0]
    val shortcode = latestMediaWithComments?.shortcode
    val media = client.getMediaByCode(shortcode)

    println("###\n" +
        "Latest with comments: \n" +
        "caption: ${media.caption} \n" +
        "likes: ${media.likeCount} \n" +
        "comments: ${media.commentCount} \n" +
        "shortcode: ${media.shortcode} \n" +
        "url: ${media.displayUrl}\n" +
        "###")

    // Get comments
    val comments: PageObject<Comment>? = client.getCommentsByMediaCode(media.shortcode, 2)
    println("### \n" +
        "Comments: ${comments?.count} \n")
    comments?.nodes?.forEach({
        println("${it.owner.username} ${it.text}")
    })
    println("###")
}