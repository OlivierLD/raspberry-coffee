package main

import (
	"fmt"
	"time"
	"oliv.cc/astro"
)

func makeTimestamp() int64 {
//     return time.Now().UnixNano() / int64(time.Millisecond)  // ms
    return time.Now().UnixNano() / 1000  // micro-s
}
/**
 * This is an example 
 * showing how to use the AstroComputer
 */
func main() {

	fmt.Println("Let's get started!")

    year := 2020
    month := 3
    day := 28
    hour := 16
    minute := 50
    second := 20

    before := makeTimestamp()
	deltaT := astro.CalculateDeltaT(year, month)
// 	fmt.Printf("DeltaT for %v-%v: %v s\n", year, month, deltaT)

	fmt.Printf("Calculating for %v-%v-%v %v:%v:%v\n", year, month, day, hour, minute, second)
	result := astro.Calculate(year, month, day, hour, minute, second, deltaT)
    after := makeTimestamp()
	fmt.Printf("Calculation took : %v \u03bcs\n", (after - before))
	fmt.Printf("Julian Dates %v %v %v\n", result.JD0h, result.JD, result.JDE)
	fmt.Printf("Sideral Time %s\n", result.SidTm)
	fmt.Printf("EoT: %v => %s\n", result.EoT, astro.OutEoT(result.EoT))

	fmt.Println("------------- Bodies --------------")
	fmt.Printf("Sun \tGHA: %s, RA: %s, Dec: %s, sd: %s, hp: %s\n", 
		astro.OutHA(result.GHASun),
		astro.OutRA(result.RASun),
		astro.OutDec(result.DECSun),
		astro.OutSdHp(result.SDSun),
		astro.OutSdHp(result.HPSun))
	fmt.Printf("Venus \tGHA: %s, RA: %s, Dec: %s, sd: %s, hp: %s\n", 
		astro.OutHA(result.GHAVenus),
		astro.OutRA(result.RAVenus),
		astro.OutDec(result.DECVenus),
		astro.OutSdHp(result.SDVenus),
		astro.OutSdHp(result.HPVenus))
	fmt.Printf("Mars \tGHA: %s, RA: %s, Dec: %s, sd: %s, hp: %s\n", 
		astro.OutHA(result.GHAMars),
		astro.OutRA(result.RAMars),
		astro.OutDec(result.DECMars),
		astro.OutSdHp(result.SDMars),
		astro.OutSdHp(result.HPMars))
	fmt.Printf("Jupiter \tGHA: %s, RA: %s, Dec: %s, sd: %s, hp: %s\n", 
		astro.OutHA(result.GHAJupiter),
		astro.OutRA(result.RAJupiter),
		astro.OutDec(result.DECJupiter),
		astro.OutSdHp(result.SDJupiter),
		astro.OutSdHp(result.HPJupiter))
	fmt.Printf("Saturn \tGHA: %s, RA: %s, Dec: %s, sd: %s, hp: %s\n", 
		astro.OutHA(result.GHASaturn),
		astro.OutRA(result.RASaturn),
		astro.OutDec(result.DECSaturn),
		astro.OutSdHp(result.SDSaturn),
		astro.OutSdHp(result.HPSaturn))

	fmt.Printf("Moon \tGHA: %s, RA: %s, Dec: %s, sd: %s, hp: %s\n", 
		astro.OutHA(result.GHAMoon),
		astro.OutRA(result.RAMoon),
		astro.OutDec(result.DECMoon),
		astro.OutSdHp(result.SDMoon),
		astro.OutSdHp(result.HPMoon))
	fmt.Printf("\tMoon Phase: %v, %s\n", result.MoonPhaseAngle, result.MoonPhase)

  fmt.Printf("Polaris\tGHA: %s, RA: %s, Dec: %s\n",
  		astro.OutHA(result.GHAPol),
  		astro.OutRA(result.RAPol),
  		astro.OutDec(result.DECPol))
  fmt.Printf("Ecliptic obliquity %s, true %s\n", astro.OutECL(result.Eps0), astro.OutECL(result.Eps))
  fmt.Printf("Lunar Distance %s\n", astro.OutHA(result.LDist))
  fmt.Printf("Day of Week %s\n", result.DoW)
  fmt.Println("---------------------------------------")
  fmt.Println("Done with Golang!")

}
