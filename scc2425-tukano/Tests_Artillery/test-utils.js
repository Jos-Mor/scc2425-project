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
    generateSearchPattern,
    registerUserIfEmpty,
    createRandomShort,
    registerUserAndCreateShortIfEmpty,
    processShortCreation,
    loadShortFromRegistered,
    prepareFollowRequest,
    prepareLikeRequest
}


const fs = require('fs')
const events = require("node:events");
const {join} = require("node:path"); // Needed for access to blobs.

var registeredUsers = []
var unregisteredUsers = []
var images = []
var videos = []
var registeredShorts = []

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

//User Tests

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
    if( response && response.body && response.body.length > 0) {
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
    if (response && response.body && response.body.length > 0) {
        let user = JSON.parse(response.body);
        registeredUsers.splice(user.userId, 1, user); //replace object at index
    }
    return next();
}

function generateSearchPattern(requestParams, context, ee, next) {
    context.pattern = "updated";    //checks for updated users
    return next();
}

function processDeleteReply(requestParams, response, context, ee, next) {
    if (response && response.body && response.body.length > 0) {
        let user = JSON.parse(response.body);
        let n = registeredUsers.findIndex(x => x.userId === user.userId); //find index of user that was removed
        registeredUsers.splice(n, 1);
    }
    return next();
}

function registerUserIfEmpty(requestParams, response, context, ee, next){
    try {
        if (registeredUsers.length === 0) {
            //initializeBaseScenario(requestParams, context, ee, next);
        }
    }catch (e){
    console.error("Error at registerUserAndCreateShortIfEmpty: " + e);
    }
}

//Short tests

function registerUserAndCreateShortIfEmpty(requestParams, response, context, ee, next){
    try {
        if (registeredUsers.length === 0) {
            //initializeBaseScenario(requestParams, context, ee, next);
        }
    }catch (e){
        console.error("Error at registerUserAndCreateShortIfEmpty: " + e);
    }
}

function createRandomShort(requestParams, response, context, ee, next){
    if (registeredUsers.length > 0) {
        let n = Math.floor(Math.random() * (registeredUsers.length)) //gets random user
        const user = registeredUsers[n];
        context.userId = user.userId;
        context.pwd = user.pwd;
    }
    return next();
}

function processShortCreation(requestParams, response, context, ee, next){
    if( response && response.body && response.body.length > 0) {
        registeredShorts.push(JSON.parse(response.body));
    }
    return next();
}

function loadShortFromRegistered(requestParams, response, context, ee, next) {
    let n = Math.floor(Math.random() * (registeredShorts.length));
    let short = registeredShorts[n];
    context.shortId = short.shortId;
    return next();
}

function prepareFollowRequest(requestParams, context, ee, next) {
    if (registeredUsers.length < 2) {
        return next();
    }

    try {
        let n1 = Math.floor(Math.random() * registeredShorts.length);
        let n2;
        do {
            n2 = Math.floor(Math.random() * registeredShorts.length);
        } while (n1 === n2);

        if (!registeredShorts[n1] || !registeredShorts[n2]) {
            console.log("No shorts available yet");
            return next();
        }

        let userId1 = registeredShorts[n1].ownerId;
        let userId2 = registeredShorts[n2].ownerId;
        context.userId1 = userId1;
        context.userId2 = userId2;
        let index = registeredUsers.findIndex(x => x.userId === userId1);
        if (index !== -1) {
            context.pwd = registeredUsers[index].pwd;
        }
        context.isFollowing = Math.random() < 0.5;

        return next();
    } catch (error) {
        console.error('Error in prepareFollowRequest:', error);
        return next(error);
    }
}

function prepareLikeRequest(requestParams, response, context, ee, next){
    let n = Math.floor(Math.random() * (registeredShorts.length));
    context.shortId = registeredShorts[n].shortId;
    n = Math.floor(Math.random() * (registeredUsers.length));
    let user = registeredUsers[n];
    context.userId = user.userId;
    context.pwd = user.pwd;
    return next();
}


/*
loadVideosFromDirectory('/data/blobsamples')

function loadVideosFromDirectory(directory) {
    try {
        const files = fs.readdirSync(directory);
        videos = files.filter(file => file.endsWith('.mp4'))
            .map(file => join(directory, file));
    } catch (error) {
        console.error("Error loading video files:", error);
    }
}
 */


/*
//creating a few base users and shorts to access
async function initializeBaseScenario(requestParams, context, ee, next) {
    if (!context) { // Initialize context if it doesn't exist
        context = {};
    }

    const makeRequest = (requestFunc, responseFunc) => {
        return new Promise((resolve) => {
            let reqParams = { headers: { 'Content-Type': 'application/json' } };
            requestFunc(reqParams, context, ee, () => {
                if (ee && typeof ee.emit === 'function') {
                    ee.emit('request', 'POST', context.currentEndpoint, context);
                }

                setTimeout(() => {
                    const responseBody = reqParams.body || "{}"; // Ensure response body is defined
                    const response = { statusCode: 200, body: responseBody };
                    if (responseFunc) {
                        responseFunc(reqParams, response, context, ee, resolve);
                    } else {
                        resolve();
                    }
                }, 1000);
            });
        });
    };

    try {
        // Create initial user if none exist
        if (registeredUsers.length === 0) {
            context.currentEndpoint = '/users/';
            await makeRequest(uploadRandomizedUser, processRegisterReply);
        }

        // Create initial short if none exist
        if (registeredShorts.length === 0 && registeredUsers.length > 0) {
            context.currentEndpoint = '/shorts/';
            await makeRequest(createRandomShort, processShortCreation);
        }

    } catch (error) {
        console.error('Error in initialization:', error);
    }
}
*/

