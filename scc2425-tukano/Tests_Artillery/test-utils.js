'use strict';

/***
 * Exported functions to be used in the testing scripts.
 */
module.exports = {
    uploadRandomizedUser,
    processRegisterReply,
    loadUserFromRegistered,
    prepareUpdateUser,
    processDeleteReply,
    processUpdateReply,
    generateSearchPattern
}


const fs = require('fs') // Needed for access to blobs.

var registeredUsers = []
var unregisteredUsers = []
var images = []

// All endpoints starting with the following prefixes will be aggregated in the same for the statistics
var statsPrefix = [ ["/rest/media/","GET"],
    ["/rest/media","POST"]
]

// Function used to compress statistics
global.myProcessEndpoint = function( str, method) {
    var i = 0;
    for( i = 0; i < statsPrefix.length; i++) {
        if( str.startsWith( statsPrefix[i][0]) && method == statsPrefix[i][1])
            return method + ":" + statsPrefix[i][0];
    }
    return method + ":" + str;
}

// Returns a random username constructed from lowercase letters.
function randomUsername(char_limit){
    const letters = 'abcdefghijklmnopqrstuvwxyz';
    let username = '';
    let num_chars = Math.floor(Math.random() * char_limit);
    for (let i = 0; i < num_chars; i++) {
        username += letters[Math.floor(Math.random() * letters.length)];
    }
    return username;
}


// Returns a random password, drawn from printable ASCII characters
function randomPassword(pass_len){
    const skip_value = 33;
    const lim_values = 94;

    let password = '';
    let num_chars = Math.floor(Math.random() * pass_len);
    for (let i = 0; i < pass_len; i++) {
        let chosen_char =  Math.floor(Math.random() * lim_values) + skip_value;
        if (chosen_char == "'" || chosen_char == '"')
            i -= 1;
        else
            password += chosen_char
    }
    return password;
}

/**
 * Process reply of the user registration.
 */
function processRegisterReply(requestParams, response, context, ee, next) {
    if( typeof response.body !== 'undefined' && response.body.length > 0) {
        let index = unregisteredUsers.findIndex(x => x.userId === response.body);
        if (index !== -1) {
            let user = {...unregisteredUsers[index]}; //create a copy of the user
            registeredUsers.push(user);
            unregisteredUsers.splice(index, 1);
        }
    }
    return next();
}

/**
 * Register a random user.
 */

function uploadRandomizedUser(requestParams, context, ee, next) {
    let username = randomUsername(10);
    let pword = randomPassword(15);
    let email = username + "@campus.fct.unl.pt";
    let displayName = username;

    const user = {
        userId: username,
        pwd: pword,
        email: email,
        displayName: username
    };
    requestParams.body = JSON.stringify(user);
    unregisteredUsers.push(user);
    return next();
}

function loadUserFromRegistered(requestParams, context, ee, next) {
    if (registeredUsers.length > 0) {
        let n = Math.floor(Math.random() * (registeredUsers.length)) //gets rando user
        const user = registeredUsers[n];
        context.userId = user.userId;
        context.pwd = user.pwd;
    }
    return next();
}

function prepareUpdateUser(requestParams, context, ee, next) {
    if (registeredUsers.length > 0) {
        let n = Math.floor(Math.random() * (registeredUsers.length)) //gets random user
        const user = registeredUsers[n];
        context.userId = user.userId;
        context.pwd = user.pwd;
        const updatedUser = {
            userId: user.userId,
            pwd: user.pwd,
            email: user.email.startsWith("updated_") ? user.email : "updated_" + user.email,
            displayName: user.displayName.startsWith("updated_") ? user.displayName : "updated_" + user.displayName
        }
        requestParams.body = JSON.stringify(updatedUser);
    }
    return next();
}

function processUpdateReply(requestParams, response, context, ee, next) {
    if (response.statusCode === 200) {
        let n = registeredUsers.findIndex(response.body.userId); //find index of user that was modified
        registeredUsers.splice(n, 1, response.body); //replace object at index
    }
    return next();
}

function generateSearchPattern(requestParams, context, ee, next) {
    context.pattern = "updated";    //checks for updated users
    return next();
}

function processDeleteReply(requestParams, response, context, ee, next) {
    if (response.statusCode === 200) {
        let n = registeredUsers.findIndex(user => user.userId === response.body.userId); //find index of user that was removed
        registeredUsers.splice(n, 1);
    }
    return next();
}