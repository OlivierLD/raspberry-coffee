#
# pygame => PYTHONPATH
# /System/Library/Frameworks/Python.framework/Versions/2.7/Extras/lib/python/py2app/recipes
#
import pygame, sys
from pygame.locals import *

# Set up game variables
window_width = 400
window_height = 300
line_thickness = 10
paddle_size = 50 # try making this smaller for a harder game
paddle_offset = 20

# Set up colour variables
black = (0 ,0 ,0 ) # variables inside brackets are 'tuples'
white = (255,255,255) # tuples are like lists but the values don't change

# Ball variables (x, y Cartesian coordinates)
# Start position middle of horizontal and vertical arena
ballX = window_width/2 - line_thickness/2
ballY = window_height/2 - line_thickness/2

# Variables to track ball direction
ballDirX = -1 ## -1 = left 1 = right
ballDirY = -1 ## -1 = up 1 = down

# Starting position in middle of game arena
playerOnePosition = (window_height - paddle_size) /2
playerTwoPosition = (window_height - paddle_size) /2

# Create rectangles for ball and paddles
paddle1 = pygame.Rect(paddle_offset,playerOnePosition, line_thickness,paddle_size)
paddle2 = pygame.Rect(window_width - paddle_offset - line_thickness, playerTwoPosition, line_thickness,paddle_size)
ball = pygame.Rect(ballX, ballY, line_thickness, line_thickness)

# Function to draw the arena
def drawArena():
    screen.fill((0,0,0))
    # Draw outline of arena
    pygame.draw.rect(screen, white, (
        (0,0),(window_width,window_height)), line_thickness*2)
    # Draw centre line
    pygame.draw.line(screen, white, (
        (int(window_width/2)),0),((int(window_width/2)),window_height), (
                         int(line_thickness/4)))

# Function to draw the paddles
def drawPaddle(paddle):
    # Stop the paddle moving too low
    if paddle.bottom > window_height - line_thickness:
        paddle.bottom = window_height- line_thickness
    # Stop the paddle moving too high
    elif paddle.top < line_thickness:
        paddle.top = line_thickness
    # Draws paddle
    pygame.draw.rect(screen, white, paddle)

# Function to draw the ball
def drawBall(ball):
    pygame.draw.rect(screen, white, ball)

# Function to move the ball
def moveBall(ball, ballDirX, ballDirY):
    ball.x += ballDirX
    ball.y += ballDirY
    return ball # returns new position

# Function checks for collision with wall and changes ball direction
def checkEdgeCollision(ball, ballDirX, ballDirY):
    if ball.top == (line_thickness) or ball.bottom == (window_height - line_thickness):
        ballDirY = ballDirY * -1
    if ball.left == (line_thickness) or ball.right == (window_width - line_thickness):
        ballDirX = ballDirX * -1
    return ballDirX, ballDirY # return new direction

# Function checks if ball has hit paddle
def checkHitBall(ball, paddle1, paddle2, ballDirX):
    if ballDirX == -1 and paddle1.right == ball.left and paddle1.top < ball.top and paddle1.bottom > ball.bottom:
        return -1 # return new direction (right)
    elif ballDirX == 1 and paddle2.left == ball.right and paddle2.top < ball.top and paddle2.bottom > ball.bottom:
        return -1 # return new direction (right)
    else:
        return 1 # return new direction (left)

# Function for AI of computer player
def artificialIntelligence(ball, ballDirX, paddle2):
    # Ball is moving away from paddle, move bat to centre
    if ballDirX == -1:
        if paddle2.centery < (window_height/2):
            paddle2.y += 1
        elif paddle2.centery > (window_height/2):
            paddle2.y -= 1
        # Ball moving towards bat, track its movement
        elif ballDirX == 1:
            if paddle2.centery < ball.centery:
                paddle2.y += 1
        else:
            paddle2.y -=1
    return paddle2

# Initialise the window
screen = pygame.display.set_mode((window_width,window_height))
pygame.display.set_caption('Pong') # Displays in the window

# Draw the arena and paddles
drawArena()
drawPaddle(paddle1)
drawPaddle(paddle2)
drawBall(ball)

# Make cursor invisible
pygame.mouse.set_visible(0)

# Main game runs in this loop
while True: # infinite loop. Press Ctrl-C to quit game
    for event in pygame.event.get():
        if event.type == QUIT:
            pygame.quit()
            sys.exit()
        # Mouse movement
        elif event.type == MOUSEMOTION:
            mousex, mousey = event.pos
            paddle1.y = mousey

    drawArena()
    drawPaddle(paddle1)
    drawPaddle(paddle2)
    drawBall(ball)

    ball = moveBall(ball, ballDirX, ballDirY)
    ballDirX, ballDirY = checkEdgeCollision(
        ball, ballDirX, ballDirY)
    ballDirX = ballDirX * checkHitBall(
        ball, paddle1, paddle2, ballDirX)
    paddle2 = artificialIntelligence (ball, ballDirX, paddle2)
    pygame.display.update()
