# The Eightball class represents the Magic 8-Ball.
class Eightball

  # Set up the available choices
  def initialize
    @choices = ["Yes", "No", "All signs point to yes", "Ask again later", "Don't bet on it"]
  end

  # Select a random choice from the available choices
  def shake
    @choices.sample
  end
end

def play
  puts "Ask the Magic 8 Ball your question > "

  # Since we don't need their answer, we won't capture it.
  gets

  # Create a new instance of the Magic 8 Ball and use it to get an answer.
  eightball = Eightball.new
  answer = eightball.shake
  puts answer

  # Prompt to restart the game and evaluate the answer.
  puts "Want to try again? Press 'y' to continue or any other key to quit."
  answer = gets.chop

  if answer == 'y'
    play
  else
    exit
  end
end

# Start the first game.
play
