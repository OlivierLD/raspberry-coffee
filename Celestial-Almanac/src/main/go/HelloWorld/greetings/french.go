package greetings

import "fmt"

func Salut(name string) string {
	// Return a greeting that embeds the name in a message.
    message := fmt.Sprintf("Salut, %v. Bienvenue !", name)
    return message
}
