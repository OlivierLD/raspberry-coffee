package spi.oled.utils;

import spi.oled.ScreenBuffer;
import spi.oled.img.ImgInterface;
import spi.oled.img.Java32x32;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Polygon;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

@SuppressWarnings("oracle.jdeveloper.java.serialversionuid-field-missing")
public class ImageBuilder
  extends java.awt.Frame
{
  private ImageBuilder instance = this;
  private LEDPanel ledPanel;
  private JPanel bottomPanel;
  private JCheckBox gridCheckBox;
  private JButton againButton;
  
  private transient ScreenBuffer sb;
  
  private static int nbCols = -1;

  private static int[] buffer = new int[512];
  static 
  {
    buffer[0] = 240;
    buffer[1] = 248;
    buffer[2] = 8;
    buffer[3] = 152;
    buffer[4] = 152;
    buffer[5] = 0;
    buffer[6] = 4;
    buffer[7] = 4;
    buffer[8] = 252;
    buffer[9] = 252;
    buffer[10] = 0;
    buffer[11] = 0;
    buffer[12] = 224;
    buffer[13] = 240;
    buffer[14] = 80;
    buffer[15] = 112;
    buffer[16] = 96;
    buffer[17] = 0;
    buffer[18] = 160;
    buffer[19] = 240;
    buffer[20] = 80;
    buffer[21] = 240;
    buffer[22] = 224;
    buffer[23] = 0;
    buffer[24] = 16;
    buffer[25] = 240;
    buffer[26] = 224;
    buffer[27] = 48;
    buffer[28] = 16;
    buffer[29] = 48;
    buffer[30] = 0;
    buffer[31] = 120;
    buffer[32] = 120;
    buffer[33] = 0;
    buffer[34] = 0;
    buffer[35] = 0;
    buffer[36] = 0;
    buffer[37] = 0;
    buffer[38] = 0;
    buffer[39] = 0;
    buffer[40] = 0;
    buffer[41] = 0;
    buffer[42] = 0;
    buffer[43] = 0;
    buffer[44] = 0;
    buffer[45] = 0;
    buffer[46] = 0;
    buffer[47] = 0;
    buffer[48] = 0;
    buffer[49] = 0;
    buffer[50] = 0;
    buffer[51] = 0;
    buffer[52] = 0;
    buffer[53] = 0;
    buffer[54] = 0;
    buffer[55] = 0;
    buffer[56] = 0;
    buffer[57] = 0;
    buffer[58] = 0;
    buffer[59] = 0;
    buffer[60] = 0;
    buffer[61] = 0;
    buffer[62] = 0;
    buffer[63] = 0;
    buffer[64] = 0;
    buffer[65] = 0;
    buffer[66] = 0;
    buffer[67] = 0;
    buffer[68] = 0;
    buffer[69] = 0;
    buffer[70] = 0;
    buffer[71] = 0;
    buffer[72] = 0;
    buffer[73] = 0;
    buffer[74] = 0;
    buffer[75] = 0;
    buffer[76] = 0;
    buffer[77] = 0;
    buffer[78] = 0;
    buffer[79] = 0;
    buffer[80] = 0;
    buffer[81] = 0;
    buffer[82] = 0;
    buffer[83] = 0;
    buffer[84] = 0;
    buffer[85] = 0;
    buffer[86] = 0;
    buffer[87] = 0;
    buffer[88] = 0;
    buffer[89] = 0;
    buffer[90] = 0;
    buffer[91] = 0;
    buffer[92] = 0;
    buffer[93] = 0;
    buffer[94] = 0;
    buffer[95] = 0;
    buffer[96] = 0;
    buffer[97] = 0;
    buffer[98] = 0;
    buffer[99] = 0;
    buffer[100] = 0;
    buffer[101] = 0;
    buffer[102] = 0;
    buffer[103] = 0;
    buffer[104] = 0;
    buffer[105] = 0;
    buffer[106] = 0;
    buffer[107] = 0;
    buffer[108] = 0;
    buffer[109] = 0;
    buffer[110] = 0;
    buffer[111] = 0;
    buffer[112] = 0;
    buffer[113] = 0;
    buffer[114] = 0;
    buffer[115] = 0;
    buffer[116] = 0;
    buffer[117] = 0;
    buffer[118] = 0;
    buffer[119] = 0;
    buffer[120] = 0;
    buffer[121] = 0;
    buffer[122] = 0;
    buffer[123] = 0;
    buffer[124] = 0;
    buffer[125] = 0;
    buffer[126] = 0;
    buffer[127] = 0;
    buffer[128] = 32;
    buffer[129] = 33;
    buffer[130] = 241;
    buffer[131] = 241;
    buffer[132] = 0;
    buffer[133] = 0;
    buffer[134] = 33;
    buffer[135] = 49;
    buffer[136] = 145;
    buffer[137] = 241;
    buffer[138] = 97;
    buffer[139] = 1;
    buffer[140] = 32;
    buffer[141] = 177;
    buffer[142] = 145;
    buffer[143] = 241;
    buffer[144] = 97;
    buffer[145] = 0;
    buffer[146] = 129;
    buffer[147] = 193;
    buffer[148] = 33;
    buffer[149] = 241;
    buffer[150] = 241;
    buffer[151] = 1;
    buffer[152] = 241;
    buffer[153] = 241;
    buffer[154] = 81;
    buffer[155] = 209;
    buffer[156] = 144;
    buffer[157] = 0;
    buffer[158] = 224;
    buffer[159] = 241;
    buffer[160] = 145;
    buffer[161] = 176;
    buffer[162] = 32;
    buffer[163] = 0;
    buffer[164] = 48;
    buffer[165] = 48;
    buffer[166] = 144;
    buffer[167] = 240;
    buffer[168] = 112;
    buffer[169] = 0;
    buffer[170] = 96;
    buffer[171] = 240;
    buffer[172] = 144;
    buffer[173] = 240;
    buffer[174] = 96;
    buffer[175] = 0;
    buffer[176] = 96;
    buffer[177] = 240;
    buffer[178] = 144;
    buffer[179] = 240;
    buffer[180] = 224;
    buffer[181] = 0;
    buffer[182] = 224;
    buffer[183] = 240;
    buffer[184] = 16;
    buffer[185] = 240;
    buffer[186] = 224;
    buffer[187] = 0;
    buffer[188] = 32;
    buffer[189] = 32;
    buffer[190] = 240;
    buffer[191] = 240;
    buffer[192] = 0;
    buffer[193] = 0;
    buffer[194] = 32;
    buffer[195] = 48;
    buffer[196] = 144;
    buffer[197] = 240;
    buffer[198] = 96;
    buffer[199] = 0;
    buffer[200] = 32;
    buffer[201] = 176;
    buffer[202] = 144;
    buffer[203] = 240;
    buffer[204] = 96;
    buffer[205] = 0;
    buffer[206] = 128;
    buffer[207] = 192;
    buffer[208] = 32;
    buffer[209] = 240;
    buffer[210] = 240;
    buffer[211] = 0;
    buffer[212] = 240;
    buffer[213] = 240;
    buffer[214] = 80;
    buffer[215] = 208;
    buffer[216] = 144;
    buffer[217] = 0;
    buffer[218] = 224;
    buffer[219] = 240;
    buffer[220] = 144;
    buffer[221] = 176;
    buffer[222] = 32;
    buffer[223] = 0;
    buffer[224] = 48;
    buffer[225] = 48;
    buffer[226] = 144;
    buffer[227] = 240;
    buffer[228] = 112;
    buffer[229] = 0;
    buffer[230] = 96;
    buffer[231] = 240;
    buffer[232] = 144;
    buffer[233] = 240;
    buffer[234] = 96;
    buffer[235] = 0;
    buffer[236] = 96;
    buffer[237] = 240;
    buffer[238] = 144;
    buffer[239] = 240;
    buffer[240] = 224;
    buffer[241] = 0;
    buffer[242] = 224;
    buffer[243] = 240;
    buffer[244] = 16;
    buffer[245] = 240;
    buffer[246] = 224;
    buffer[247] = 0;
    buffer[248] = 32;
    buffer[249] = 32;
    buffer[250] = 240;
    buffer[251] = 240;
    buffer[252] = 0;
    buffer[253] = 0;
    buffer[254] = 32;
    buffer[255] = 48;
    buffer[256] = 4;
    buffer[257] = 4;
    buffer[258] = 7;
    buffer[259] = 7;
    buffer[260] = 4;
    buffer[261] = 4;
    buffer[262] = 6;
    buffer[263] = 7;
    buffer[264] = 5;
    buffer[265] = 6;
    buffer[266] = 6;
    buffer[267] = 0;
    buffer[268] = 2;
    buffer[269] = 6;
    buffer[270] = 4;
    buffer[271] = 7;
    buffer[272] = 3;
    buffer[273] = 0;
    buffer[274] = 1;
    buffer[275] = 1;
    buffer[276] = 1;
    buffer[277] = 7;
    buffer[278] = 7;
    buffer[279] = 1;
    buffer[280] = 6;
    buffer[281] = 4;
    buffer[282] = 4;
    buffer[283] = 7;
    buffer[284] = 3;
    buffer[285] = 0;
    buffer[286] = 3;
    buffer[287] = 7;
    buffer[288] = 4;
    buffer[289] = 7;
    buffer[290] = 3;
    buffer[291] = 0;
    buffer[292] = 0;
    buffer[293] = 6;
    buffer[294] = 7;
    buffer[295] = 1;
    buffer[296] = 0;
    buffer[297] = 0;
    buffer[298] = 3;
    buffer[299] = 7;
    buffer[300] = 4;
    buffer[301] = 7;
    buffer[302] = 3;
    buffer[303] = 0;
    buffer[304] = 2;
    buffer[305] = 6;
    buffer[306] = 4;
    buffer[307] = 7;
    buffer[308] = 3;
    buffer[309] = 0;
    buffer[310] = 131;
    buffer[311] = 135;
    buffer[312] = 196;
    buffer[313] = 199;
    buffer[314] = 3;
    buffer[315] = 0;
    buffer[316] = 4;
    buffer[317] = 4;
    buffer[318] = 7;
    buffer[319] = 7;
    buffer[320] = 4;
    buffer[321] = 4;
    buffer[322] = 6;
    buffer[323] = 7;
    buffer[324] = 5;
    buffer[325] = 6;
    buffer[326] = 6;
    buffer[327] = 0;
    buffer[328] = 2;
    buffer[329] = 6;
    buffer[330] = 4;
    buffer[331] = 7;
    buffer[332] = 3;
    buffer[333] = 0;
    buffer[334] = 1;
    buffer[335] = 1;
    buffer[336] = 1;
    buffer[337] = 7;
    buffer[338] = 7;
    buffer[339] = 1;
    buffer[340] = 6;
    buffer[341] = 4;
    buffer[342] = 4;
    buffer[343] = 7;
    buffer[344] = 3;
    buffer[345] = 0;
    buffer[346] = 3;
    buffer[347] = 7;
    buffer[348] = 4;
    buffer[349] = 7;
    buffer[350] = 3;
    buffer[351] = 0;
    buffer[352] = 0;
    buffer[353] = 6;
    buffer[354] = 7;
    buffer[355] = 1;
    buffer[356] = 0;
    buffer[357] = 0;
    buffer[358] = 3;
    buffer[359] = 7;
    buffer[360] = 4;
    buffer[361] = 7;
    buffer[362] = 3;
    buffer[363] = 0;
    buffer[364] = 2;
    buffer[365] = 6;
    buffer[366] = 4;
    buffer[367] = 7;
    buffer[368] = 3;
    buffer[369] = 0;
    buffer[370] = 131;
    buffer[371] = 199;
    buffer[372] = 68;
    buffer[373] = 199;
    buffer[374] = 131;
    buffer[375] = 0;
    buffer[376] = 4;
    buffer[377] = 4;
    buffer[378] = 7;
    buffer[379] = 7;
    buffer[380] = 4;
    buffer[381] = 4;
    buffer[382] = 6;
    buffer[383] = 7;
    buffer[384] = 0;
    buffer[385] = 0;
    buffer[386] = 0;
    buffer[387] = 0;
    buffer[388] = 0;
    buffer[389] = 0;
    buffer[390] = 0;
    buffer[391] = 0;
    buffer[392] = 0;
    buffer[393] = 0;
    buffer[394] = 0;
    buffer[395] = 0;
    buffer[396] = 0;
    buffer[397] = 0;
    buffer[398] = 0;
    buffer[399] = 0;
    buffer[400] = 0;
    buffer[401] = 0;
    buffer[402] = 0;
    buffer[403] = 0;
    buffer[404] = 0;
    buffer[405] = 0;
    buffer[406] = 0;
    buffer[407] = 0;
    buffer[408] = 0;
    buffer[409] = 0;
    buffer[410] = 0;
    buffer[411] = 0;
    buffer[412] = 0;
    buffer[413] = 0;
    buffer[414] = 0;
    buffer[415] = 0;
    buffer[416] = 0;
    buffer[417] = 0;
    buffer[418] = 0;
    buffer[419] = 0;
    buffer[420] = 0;
    buffer[421] = 0;
    buffer[422] = 0;
    buffer[423] = 0;
    buffer[424] = 0;
    buffer[425] = 0;
    buffer[426] = 0;
    buffer[427] = 0;
    buffer[428] = 0;
    buffer[429] = 0;
    buffer[430] = 0;
    buffer[431] = 0;
    buffer[432] = 0;
    buffer[433] = 0;
    buffer[434] = 0;
    buffer[435] = 0;
    buffer[436] = 0;
    buffer[437] = 0;
    buffer[438] = 16;
    buffer[439] = 16;
    buffer[440] = 31;
    buffer[441] = 31;
    buffer[442] = 16;
    buffer[443] = 16;
    buffer[444] = 0;
    buffer[445] = 0;
    buffer[446] = 0;
    buffer[447] = 0;
    buffer[448] = 0;
    buffer[449] = 0;
    buffer[450] = 0;
    buffer[451] = 0;
    buffer[452] = 0;
    buffer[453] = 0;
    buffer[454] = 0;
    buffer[455] = 0;
    buffer[456] = 0;
    buffer[457] = 0;
    buffer[458] = 0;
    buffer[459] = 0;
    buffer[460] = 0;
    buffer[461] = 0;
    buffer[462] = 0;
    buffer[463] = 0;
    buffer[464] = 0;
    buffer[465] = 0;
    buffer[466] = 0;
    buffer[467] = 0;
    buffer[468] = 0;
    buffer[469] = 0;
    buffer[470] = 0;
    buffer[471] = 0;
    buffer[472] = 0;
    buffer[473] = 0;
    buffer[474] = 0;
    buffer[475] = 0;
    buffer[476] = 0;
    buffer[477] = 0;
    buffer[478] = 0;
    buffer[479] = 0;
    buffer[480] = 0;
    buffer[481] = 0;
    buffer[482] = 0;
    buffer[483] = 0;
    buffer[484] = 0;
    buffer[485] = 0;
    buffer[486] = 0;
    buffer[487] = 0;
    buffer[488] = 0;
    buffer[489] = 0;
    buffer[490] = 0;
    buffer[491] = 0;
    buffer[492] = 0;
    buffer[493] = 0;
    buffer[494] = 0;
    buffer[495] = 0;
    buffer[496] = 0;
    buffer[497] = 0;
    buffer[498] = 24;
    buffer[499] = 28;
    buffer[500] = 22;
    buffer[501] = 27;
    buffer[502] = 25;
    buffer[503] = 0;
    buffer[504] = 0;
    buffer[505] = 0;
    buffer[506] = 0;
    buffer[507] = 0;
    buffer[508] = 0;
    buffer[509] = 0;
    buffer[510] = 0;
    buffer[511] = 0;
  }
  
  private static int[] buffer1 = new int[512];
  static
  {
    buffer1[0] = 0;
    buffer1[1] = 120;
    buffer1[2] = 120;
    buffer1[3] = 0;
    buffer1[4] = 0;
    buffer1[5] = 0;
    buffer1[6] = 248;
    buffer1[7] = 12;
    buffer1[8] = 100;
    buffer1[9] = 148;
    buffer1[10] = 248;
    buffer1[11] = 128;
    buffer1[12] = 144;
    buffer1[13] = 252;
    buffer1[14] = 144;
    buffer1[15] = 252;
    buffer1[16] = 144;
    buffer1[17] = 0;
    buffer1[18] = 152;
    buffer1[19] = 188;
    buffer1[20] = 54;
    buffer1[21] = 244;
    buffer1[22] = 236;
    buffer1[23] = 0;
    buffer1[24] = 156;
    buffer1[25] = 84;
    buffer1[26] = 252;
    buffer1[27] = 80;
    buffer1[28] = 200;
    buffer1[29] = 0;
    buffer1[30] = 16;
    buffer1[31] = 24;
    buffer1[32] = 12;
    buffer1[33] = 24;
    buffer1[34] = 16;
    buffer1[35] = 128;
    buffer1[36] = 208;
    buffer1[37] = 120;
    buffer1[38] = 232;
    buffer1[39] = 200;
    buffer1[40] = 64;
    buffer1[41] = 0;
    buffer1[42] = 40;
    buffer1[43] = 24;
    buffer1[44] = 28;
    buffer1[45] = 40;
    buffer1[46] = 0;
    buffer1[47] = 0;
    buffer1[48] = 0;
    buffer1[49] = 240;
    buffer1[50] = 248;
    buffer1[51] = 4;
    buffer1[52] = 0;
    buffer1[53] = 0;
    buffer1[54] = 0;
    buffer1[55] = 4;
    buffer1[56] = 248;
    buffer1[57] = 240;
    buffer1[58] = 0;
    buffer1[59] = 0;
    buffer1[60] = 0;
    buffer1[61] = 0;
    buffer1[62] = 0;
    buffer1[63] = 0;
    buffer1[64] = 0;
    buffer1[65] = 0;
    buffer1[66] = 32;
    buffer1[67] = 32;
    buffer1[68] = 248;
    buffer1[69] = 32;
    buffer1[70] = 32;
    buffer1[71] = 0;
    buffer1[72] = 0;
    buffer1[73] = 32;
    buffer1[74] = 248;
    buffer1[75] = 220;
    buffer1[76] = 4;
    buffer1[77] = 0;
    buffer1[78] = 4;
    buffer1[79] = 220;
    buffer1[80] = 248;
    buffer1[81] = 32;
    buffer1[82] = 0;
    buffer1[83] = 0;
    buffer1[84] = 0;
    buffer1[85] = 0;
    buffer1[86] = 248;
    buffer1[87] = 0;
    buffer1[88] = 0;
    buffer1[89] = 0;
    buffer1[90] = 0;
    buffer1[91] = 252;
    buffer1[92] = 252;
    buffer1[93] = 4;
    buffer1[94] = 0;
    buffer1[95] = 0;
    buffer1[96] = 0;
    buffer1[97] = 4;
    buffer1[98] = 252;
    buffer1[99] = 252;
    buffer1[100] = 0;
    buffer1[101] = 0;
    buffer1[102] = 12;
    buffer1[103] = 48;
    buffer1[104] = 192;
    buffer1[105] = 0;
    buffer1[106] = 0;
    buffer1[107] = 0;
    buffer1[108] = 32;
    buffer1[109] = 32;
    buffer1[110] = 32;
    buffer1[111] = 32;
    buffer1[112] = 32;
    buffer1[113] = 0;
    buffer1[114] = 80;
    buffer1[115] = 80;
    buffer1[116] = 80;
    buffer1[117] = 80;
    buffer1[118] = 0;
    buffer1[119] = 0;
    buffer1[120] = 0;
    buffer1[121] = 0;
    buffer1[122] = 0;
    buffer1[123] = 0;
    buffer1[124] = 0;
    buffer1[125] = 0;
    buffer1[126] = 0;
    buffer1[127] = 0;
    buffer1[128] = 0;
    buffer1[129] = 129;
    buffer1[130] = 129;
    buffer1[131] = 0;
    buffer1[132] = 0;
    buffer1[133] = 0;
    buffer1[134] = 1;
    buffer1[135] = 67;
    buffer1[136] = 50;
    buffer1[137] = 18;
    buffer1[138] = 0;
    buffer1[139] = 0;
    buffer1[140] = 128;
    buffer1[141] = 195;
    buffer1[142] = 96;
    buffer1[143] = 35;
    buffer1[144] = 0;
    buffer1[145] = 0;
    buffer1[146] = 1;
    buffer1[147] = 33;
    buffer1[148] = 99;
    buffer1[149] = 193;
    buffer1[150] = 128;
    buffer1[151] = 0;
    buffer1[152] = 0;
    buffer1[153] = 0;
    buffer1[154] = 1;
    buffer1[155] = 1;
    buffer1[156] = 1;
    buffer1[157] = 0;
    buffer1[158] = 0;
    buffer1[159] = 0;
    buffer1[160] = 0;
    buffer1[161] = 0;
    buffer1[162] = 0;
    buffer1[163] = 0;
    buffer1[164] = 65;
    buffer1[165] = 33;
    buffer1[166] = 161;
    buffer1[167] = 225;
    buffer1[168] = 65;
    buffer1[169] = 0;
    buffer1[170] = 0;
    buffer1[171] = 0;
    buffer1[172] = 0;
    buffer1[173] = 192;
    buffer1[174] = 48;
    buffer1[175] = 0;
    buffer1[176] = 0;
    buffer1[177] = 128;
    buffer1[178] = 129;
    buffer1[179] = 2;
    buffer1[180] = 0;
    buffer1[181] = 0;
    buffer1[182] = 0;
    buffer1[183] = 226;
    buffer1[184] = 1;
    buffer1[185] = 224;
    buffer1[186] = 0;
    buffer1[187] = 0;
    buffer1[188] = 36;
    buffer1[189] = 228;
    buffer1[190] = 100;
    buffer1[191] = 228;
    buffer1[192] = 4;
    buffer1[193] = 36;
    buffer1[194] = 224;
    buffer1[195] = 224;
    buffer1[196] = 160;
    buffer1[197] = 224;
    buffer1[198] = 64;
    buffer1[199] = 0;
    buffer1[200] = 192;
    buffer1[201] = 224;
    buffer1[202] = 33;
    buffer1[203] = 99;
    buffer1[204] = 98;
    buffer1[205] = 32;
    buffer1[206] = 226;
    buffer1[207] = 227;
    buffer1[208] = 33;
    buffer1[209] = 224;
    buffer1[210] = 192;
    buffer1[211] = 32;
    buffer1[212] = 224;
    buffer1[213] = 224;
    buffer1[214] = 163;
    buffer1[215] = 160;
    buffer1[216] = 32;
    buffer1[217] = 32;
    buffer1[218] = 224;
    buffer1[219] = 227;
    buffer1[220] = 163;
    buffer1[221] = 162;
    buffer1[222] = 32;
    buffer1[223] = 0;
    buffer1[224] = 192;
    buffer1[225] = 226;
    buffer1[226] = 35;
    buffer1[227] = 99;
    buffer1[228] = 64;
    buffer1[229] = 32;
    buffer1[230] = 224;
    buffer1[231] = 224;
    buffer1[232] = 128;
    buffer1[233] = 227;
    buffer1[234] = 224;
    buffer1[235] = 32;
    buffer1[236] = 32;
    buffer1[237] = 224;
    buffer1[238] = 224;
    buffer1[239] = 32;
    buffer1[240] = 0;
    buffer1[241] = 0;
    buffer1[242] = 0;
    buffer1[243] = 0;
    buffer1[244] = 0;
    buffer1[245] = 0;
    buffer1[246] = 0;
    buffer1[247] = 0;
    buffer1[248] = 0;
    buffer1[249] = 0;
    buffer1[250] = 0;
    buffer1[251] = 0;
    buffer1[252] = 0;
    buffer1[253] = 0;
    buffer1[254] = 0;
    buffer1[255] = 0;
    buffer1[256] = 0;
    buffer1[257] = 132;
    buffer1[258] = 132;
    buffer1[259] = 128;
    buffer1[260] = 128;
    buffer1[261] = 128;
    buffer1[262] = 128;
    buffer1[263] = 128;
    buffer1[264] = 0;
    buffer1[265] = 128;
    buffer1[266] = 128;
    buffer1[267] = 128;
    buffer1[268] = 128;
    buffer1[269] = 129;
    buffer1[270] = 131;
    buffer1[271] = 2;
    buffer1[272] = 0;
    buffer1[273] = 128;
    buffer1[274] = 128;
    buffer1[275] = 2;
    buffer1[276] = 3;
    buffer1[277] = 1;
    buffer1[278] = 128;
    buffer1[279] = 128;
    buffer1[280] = 128;
    buffer1[281] = 144;
    buffer1[282] = 12;
    buffer1[283] = 132;
    buffer1[284] = 128;
    buffer1[285] = 128;
    buffer1[286] = 0;
    buffer1[287] = 132;
    buffer1[288] = 132;
    buffer1[289] = 128;
    buffer1[290] = 0;
    buffer1[291] = 128;
    buffer1[292] = 128;
    buffer1[293] = 133;
    buffer1[294] = 133;
    buffer1[295] = 128;
    buffer1[296] = 0;
    buffer1[297] = 0;
    buffer1[298] = 0;
    buffer1[299] = 140;
    buffer1[300] = 131;
    buffer1[301] = 128;
    buffer1[302] = 0;
    buffer1[303] = 128;
    buffer1[304] = 144;
    buffer1[305] = 140;
    buffer1[306] = 132;
    buffer1[307] = 128;
    buffer1[308] = 0;
    buffer1[309] = 0;
    buffer1[310] = 0;
    buffer1[311] = 128;
    buffer1[312] = 128;
    buffer1[313] = 128;
    buffer1[314] = 128;
    buffer1[315] = 132;
    buffer1[316] = 135;
    buffer1[317] = 135;
    buffer1[318] = 129;
    buffer1[319] = 135;
    buffer1[320] = 135;
    buffer1[321] = 132;
    buffer1[322] = 135;
    buffer1[323] = 135;
    buffer1[324] = 4;
    buffer1[325] = 135;
    buffer1[326] = 131;
    buffer1[327] = 128;
    buffer1[328] = 131;
    buffer1[329] = 135;
    buffer1[330] = 4;
    buffer1[331] = 134;
    buffer1[332] = 130;
    buffer1[333] = 132;
    buffer1[334] = 135;
    buffer1[335] = 7;
    buffer1[336] = 132;
    buffer1[337] = 7;
    buffer1[338] = 131;
    buffer1[339] = 132;
    buffer1[340] = 135;
    buffer1[341] = 135;
    buffer1[342] = 4;
    buffer1[343] = 6;
    buffer1[344] = 134;
    buffer1[345] = 132;
    buffer1[346] = 135;
    buffer1[347] = 135;
    buffer1[348] = 4;
    buffer1[349] = 0;
    buffer1[350] = 128;
    buffer1[351] = 128;
    buffer1[352] = 131;
    buffer1[353] = 135;
    buffer1[354] = 133;
    buffer1[355] = 135;
    buffer1[356] = 135;
    buffer1[357] = 4;
    buffer1[358] = 7;
    buffer1[359] = 7;
    buffer1[360] = 0;
    buffer1[361] = 7;
    buffer1[362] = 7;
    buffer1[363] = 68;
    buffer1[364] = 196;
    buffer1[365] = 199;
    buffer1[366] = 7;
    buffer1[367] = 4;
    buffer1[368] = 0;
    buffer1[369] = 0;
    buffer1[370] = 0;
    buffer1[371] = 0;
    buffer1[372] = 0;
    buffer1[373] = 0;
    buffer1[374] = 0;
    buffer1[375] = 0;
    buffer1[376] = 0;
    buffer1[377] = 0;
    buffer1[378] = 0;
    buffer1[379] = 0;
    buffer1[380] = 0;
    buffer1[381] = 0;
    buffer1[382] = 0;
    buffer1[383] = 0;
    buffer1[384] = 28;
    buffer1[385] = 16;
    buffer1[386] = 31;
    buffer1[387] = 15;
    buffer1[388] = 0;
    buffer1[389] = 16;
    buffer1[390] = 31;
    buffer1[391] = 31;
    buffer1[392] = 22;
    buffer1[393] = 13;
    buffer1[394] = 24;
    buffer1[395] = 16;
    buffer1[396] = 31;
    buffer1[397] = 31;
    buffer1[398] = 16;
    buffer1[399] = 24;
    buffer1[400] = 24;
    buffer1[401] = 16;
    buffer1[402] = 31;
    buffer1[403] = 7;
    buffer1[404] = 28;
    buffer1[405] = 7;
    buffer1[406] = 31;
    buffer1[407] = 16;
    buffer1[408] = 31;
    buffer1[409] = 31;
    buffer1[410] = 3;
    buffer1[411] = 12;
    buffer1[412] = 31;
    buffer1[413] = 0;
    buffer1[414] = 15;
    buffer1[415] = 31;
    buffer1[416] = 16;
    buffer1[417] = 31;
    buffer1[418] = 15;
    buffer1[419] = 16;
    buffer1[420] = 31;
    buffer1[421] = 31;
    buffer1[422] = 20;
    buffer1[423] = 7;
    buffer1[424] = 3;
    buffer1[425] = 0;
    buffer1[426] = 15;
    buffer1[427] = 31;
    buffer1[428] = 16;
    buffer1[429] = 63;
    buffer1[430] = 47;
    buffer1[431] = 16;
    buffer1[432] = 31;
    buffer1[433] = 31;
    buffer1[434] = 20;
    buffer1[435] = 15;
    buffer1[436] = 27;
    buffer1[437] = 16;
    buffer1[438] = 27;
    buffer1[439] = 19;
    buffer1[440] = 22;
    buffer1[441] = 30;
    buffer1[442] = 13;
    buffer1[443] = 1;
    buffer1[444] = 16;
    buffer1[445] = 31;
    buffer1[446] = 31;
    buffer1[447] = 16;
    buffer1[448] = 1;
    buffer1[449] = 0;
    buffer1[450] = 15;
    buffer1[451] = 31;
    buffer1[452] = 16;
    buffer1[453] = 31;
    buffer1[454] = 15;
    buffer1[455] = 0;
    buffer1[456] = 1;
    buffer1[457] = 15;
    buffer1[458] = 28;
    buffer1[459] = 15;
    buffer1[460] = 1;
    buffer1[461] = 0;
    buffer1[462] = 7;
    buffer1[463] = 28;
    buffer1[464] = 15;
    buffer1[465] = 28;
    buffer1[466] = 7;
    buffer1[467] = 0;
    buffer1[468] = 16;
    buffer1[469] = 25;
    buffer1[470] = 15;
    buffer1[471] = 15;
    buffer1[472] = 25;
    buffer1[473] = 0;
    buffer1[474] = 1;
    buffer1[475] = 19;
    buffer1[476] = 30;
    buffer1[477] = 30;
    buffer1[478] = 19;
    buffer1[479] = 1;
    buffer1[480] = 25;
    buffer1[481] = 29;
    buffer1[482] = 22;
    buffer1[483] = 27;
    buffer1[484] = 25;
    buffer1[485] = 0;
    buffer1[486] = 26;
    buffer1[487] = 31;
    buffer1[488] = 21;
    buffer1[489] = 31;
    buffer1[490] = 30;
    buffer1[491] = 16;
    buffer1[492] = 31;
    buffer1[493] = 31;
    buffer1[494] = 17;
    buffer1[495] = 31;
    buffer1[496] = 14;
    buffer1[497] = 0;
    buffer1[498] = 14;
    buffer1[499] = 31;
    buffer1[500] = 17;
    buffer1[501] = 27;
    buffer1[502] = 10;
    buffer1[503] = 0;
    buffer1[504] = 0;
    buffer1[505] = 0;
    buffer1[506] = 0;
    buffer1[507] = 0;
    buffer1[508] = 0;
    buffer1[509] = 0;
    buffer1[510] = 0;
    buffer1[511] = 0;
  }
  
  private static int[] buffer2 = new int[512];
  static
  {
    buffer2[0] = 0;
    buffer2[1] = 120;
    buffer2[2] = 120;
    buffer2[3] = 0;
    buffer2[4] = 0;
    buffer2[5] = 0;
    buffer2[6] = 248;
    buffer2[7] = 12;
    buffer2[8] = 100;
    buffer2[9] = 148;
    buffer2[10] = 248;
    buffer2[11] = 128;
    buffer2[12] = 144;
    buffer2[13] = 252;
    buffer2[14] = 144;
    buffer2[15] = 252;
    buffer2[16] = 144;
    buffer2[17] = 0;
    buffer2[18] = 152;
    buffer2[19] = 188;
    buffer2[20] = 54;
    buffer2[21] = 244;
    buffer2[22] = 236;
    buffer2[23] = 0;
    buffer2[24] = 156;
    buffer2[25] = 84;
    buffer2[26] = 252;
    buffer2[27] = 80;
    buffer2[28] = 200;
    buffer2[29] = 0;
    buffer2[30] = 16;
    buffer2[31] = 24;
    buffer2[32] = 12;
    buffer2[33] = 24;
    buffer2[34] = 16;
    buffer2[35] = 128;
    buffer2[36] = 208;
    buffer2[37] = 120;
    buffer2[38] = 232;
    buffer2[39] = 200;
    buffer2[40] = 64;
    buffer2[41] = 0;
    buffer2[42] = 40;
    buffer2[43] = 24;
    buffer2[44] = 28;
    buffer2[45] = 40;
    buffer2[46] = 0;
    buffer2[47] = 0;
    buffer2[48] = 0;
    buffer2[49] = 240;
    buffer2[50] = 248;
    buffer2[51] = 4;
    buffer2[52] = 0;
    buffer2[53] = 0;
    buffer2[54] = 0;
    buffer2[55] = 4;
    buffer2[56] = 248;
    buffer2[57] = 240;
    buffer2[58] = 0;
    buffer2[59] = 0;
    buffer2[60] = 0;
    buffer2[61] = 0;
    buffer2[62] = 0;
    buffer2[63] = 0;
    buffer2[64] = 0;
    buffer2[65] = 0;
    buffer2[66] = 32;
    buffer2[67] = 32;
    buffer2[68] = 248;
    buffer2[69] = 32;
    buffer2[70] = 32;
    buffer2[71] = 0;
    buffer2[72] = 0;
    buffer2[73] = 32;
    buffer2[74] = 248;
    buffer2[75] = 220;
    buffer2[76] = 4;
    buffer2[77] = 0;
    buffer2[78] = 4;
    buffer2[79] = 220;
    buffer2[80] = 248;
    buffer2[81] = 32;
    buffer2[82] = 0;
    buffer2[83] = 0;
    buffer2[84] = 0;
    buffer2[85] = 0;
    buffer2[86] = 248;
    buffer2[87] = 0;
    buffer2[88] = 0;
    buffer2[89] = 0;
    buffer2[90] = 0;
    buffer2[91] = 252;
    buffer2[92] = 252;
    buffer2[93] = 4;
    buffer2[94] = 0;
    buffer2[95] = 0;
    buffer2[96] = 0;
    buffer2[97] = 4;
    buffer2[98] = 252;
    buffer2[99] = 252;
    buffer2[100] = 0;
    buffer2[101] = 0;
    buffer2[102] = 12;
    buffer2[103] = 48;
    buffer2[104] = 192;
    buffer2[105] = 0;
    buffer2[106] = 0;
    buffer2[107] = 0;
    buffer2[108] = 32;
    buffer2[109] = 32;
    buffer2[110] = 32;
    buffer2[111] = 32;
    buffer2[112] = 32;
    buffer2[113] = 0;
    buffer2[114] = 80;
    buffer2[115] = 80;
    buffer2[116] = 80;
    buffer2[117] = 80;
    buffer2[118] = 0;
    buffer2[119] = 0;
    buffer2[120] = 0;
    buffer2[121] = 0;
    buffer2[122] = 0;
    buffer2[123] = 0;
    buffer2[124] = 0;
    buffer2[125] = 0;
    buffer2[126] = 0;
    buffer2[127] = 0;
    buffer2[128] = 128;
    buffer2[129] = 193;
    buffer2[130] = 81;
    buffer2[131] = 240;
    buffer2[132] = 240;
    buffer2[133] = 0;
    buffer2[134] = 129;
    buffer2[135] = 195;
    buffer2[136] = 66;
    buffer2[137] = 194;
    buffer2[138] = 128;
    buffer2[139] = 0;
    buffer2[140] = 64;
    buffer2[141] = 227;
    buffer2[142] = 240;
    buffer2[143] = 83;
    buffer2[144] = 80;
    buffer2[145] = 0;
    buffer2[146] = 129;
    buffer2[147] = 193;
    buffer2[148] = 67;
    buffer2[149] = 129;
    buffer2[150] = 192;
    buffer2[151] = 16;
    buffer2[152] = 240;
    buffer2[153] = 240;
    buffer2[154] = 65;
    buffer2[155] = 193;
    buffer2[156] = 129;
    buffer2[157] = 0;
    buffer2[158] = 64;
    buffer2[159] = 64;
    buffer2[160] = 208;
    buffer2[161] = 208;
    buffer2[162] = 0;
    buffer2[163] = 0;
    buffer2[164] = 65;
    buffer2[165] = 65;
    buffer2[166] = 209;
    buffer2[167] = 209;
    buffer2[168] = 1;
    buffer2[169] = 16;
    buffer2[170] = 240;
    buffer2[171] = 240;
    buffer2[172] = 128;
    buffer2[173] = 192;
    buffer2[174] = 64;
    buffer2[175] = 0;
    buffer2[176] = 16;
    buffer2[177] = 16;
    buffer2[178] = 241;
    buffer2[179] = 242;
    buffer2[180] = 0;
    buffer2[181] = 64;
    buffer2[182] = 192;
    buffer2[183] = 194;
    buffer2[184] = 193;
    buffer2[185] = 192;
    buffer2[186] = 128;
    buffer2[187] = 64;
    buffer2[188] = 196;
    buffer2[189] = 132;
    buffer2[190] = 68;
    buffer2[191] = 196;
    buffer2[192] = 132;
    buffer2[193] = 4;
    buffer2[194] = 128;
    buffer2[195] = 192;
    buffer2[196] = 64;
    buffer2[197] = 192;
    buffer2[198] = 128;
    buffer2[199] = 64;
    buffer2[200] = 192;
    buffer2[201] = 192;
    buffer2[202] = 65;
    buffer2[203] = 195;
    buffer2[204] = 130;
    buffer2[205] = 0;
    buffer2[206] = 130;
    buffer2[207] = 195;
    buffer2[208] = 65;
    buffer2[209] = 128;
    buffer2[210] = 192;
    buffer2[211] = 64;
    buffer2[212] = 64;
    buffer2[213] = 192;
    buffer2[214] = 131;
    buffer2[215] = 192;
    buffer2[216] = 64;
    buffer2[217] = 192;
    buffer2[218] = 128;
    buffer2[219] = 195;
    buffer2[220] = 195;
    buffer2[221] = 66;
    buffer2[222] = 64;
    buffer2[223] = 0;
    buffer2[224] = 64;
    buffer2[225] = 242;
    buffer2[226] = 243;
    buffer2[227] = 67;
    buffer2[228] = 64;
    buffer2[229] = 64;
    buffer2[230] = 192;
    buffer2[231] = 192;
    buffer2[232] = 0;
    buffer2[233] = 195;
    buffer2[234] = 192;
    buffer2[235] = 64;
    buffer2[236] = 192;
    buffer2[237] = 192;
    buffer2[238] = 0;
    buffer2[239] = 192;
    buffer2[240] = 192;
    buffer2[241] = 64;
    buffer2[242] = 192;
    buffer2[243] = 0;
    buffer2[244] = 192;
    buffer2[245] = 0;
    buffer2[246] = 192;
    buffer2[247] = 64;
    buffer2[248] = 0;
    buffer2[249] = 0;
    buffer2[250] = 0;
    buffer2[251] = 0;
    buffer2[252] = 0;
    buffer2[253] = 0;
    buffer2[254] = 0;
    buffer2[255] = 0;
    buffer2[256] = 3;
    buffer2[257] = 7;
    buffer2[258] = 4;
    buffer2[259] = 7;
    buffer2[260] = 7;
    buffer2[261] = 4;
    buffer2[262] = 3;
    buffer2[263] = 7;
    buffer2[264] = 5;
    buffer2[265] = 5;
    buffer2[266] = 5;
    buffer2[267] = 0;
    buffer2[268] = 4;
    buffer2[269] = 7;
    buffer2[270] = 7;
    buffer2[271] = 4;
    buffer2[272] = 4;
    buffer2[273] = 0;
    buffer2[274] = 19;
    buffer2[275] = 23;
    buffer2[276] = 20;
    buffer2[277] = 31;
    buffer2[278] = 15;
    buffer2[279] = 0;
    buffer2[280] = 7;
    buffer2[281] = 7;
    buffer2[282] = 0;
    buffer2[283] = 7;
    buffer2[284] = 7;
    buffer2[285] = 0;
    buffer2[286] = 4;
    buffer2[287] = 4;
    buffer2[288] = 7;
    buffer2[289] = 7;
    buffer2[290] = 4;
    buffer2[291] = 4;
    buffer2[292] = 16;
    buffer2[293] = 16;
    buffer2[294] = 31;
    buffer2[295] = 15;
    buffer2[296] = 0;
    buffer2[297] = 0;
    buffer2[298] = 7;
    buffer2[299] = 7;
    buffer2[300] = 3;
    buffer2[301] = 6;
    buffer2[302] = 4;
    buffer2[303] = 4;
    buffer2[304] = 4;
    buffer2[305] = 4;
    buffer2[306] = 7;
    buffer2[307] = 7;
    buffer2[308] = 4;
    buffer2[309] = 0;
    buffer2[310] = 7;
    buffer2[311] = 0;
    buffer2[312] = 7;
    buffer2[313] = 0;
    buffer2[314] = 7;
    buffer2[315] = 0;
    buffer2[316] = 7;
    buffer2[317] = 7;
    buffer2[318] = 0;
    buffer2[319] = 7;
    buffer2[320] = 7;
    buffer2[321] = 0;
    buffer2[322] = 3;
    buffer2[323] = 7;
    buffer2[324] = 4;
    buffer2[325] = 7;
    buffer2[326] = 3;
    buffer2[327] = 16;
    buffer2[328] = 31;
    buffer2[329] = 31;
    buffer2[330] = 20;
    buffer2[331] = 7;
    buffer2[332] = 3;
    buffer2[333] = 0;
    buffer2[334] = 3;
    buffer2[335] = 7;
    buffer2[336] = 20;
    buffer2[337] = 31;
    buffer2[338] = 31;
    buffer2[339] = 16;
    buffer2[340] = 4;
    buffer2[341] = 7;
    buffer2[342] = 7;
    buffer2[343] = 4;
    buffer2[344] = 0;
    buffer2[345] = 0;
    buffer2[346] = 4;
    buffer2[347] = 5;
    buffer2[348] = 5;
    buffer2[349] = 7;
    buffer2[350] = 7;
    buffer2[351] = 2;
    buffer2[352] = 0;
    buffer2[353] = 3;
    buffer2[354] = 7;
    buffer2[355] = 4;
    buffer2[356] = 6;
    buffer2[357] = 0;
    buffer2[358] = 3;
    buffer2[359] = 7;
    buffer2[360] = 4;
    buffer2[361] = 7;
    buffer2[362] = 7;
    buffer2[363] = 0;
    buffer2[364] = 0;
    buffer2[365] = 3;
    buffer2[366] = 7;
    buffer2[367] = 3;
    buffer2[368] = 0;
    buffer2[369] = 0;
    buffer2[370] = 1;
    buffer2[371] = 7;
    buffer2[372] = 3;
    buffer2[373] = 7;
    buffer2[374] = 3;
    buffer2[375] = 0;
    buffer2[376] = 0;
    buffer2[377] = 0;
    buffer2[378] = 0;
    buffer2[379] = 0;
    buffer2[380] = 0;
    buffer2[381] = 0;
    buffer2[382] = 0;
    buffer2[383] = 0;
    buffer2[384] = 17;
    buffer2[385] = 27;
    buffer2[386] = 15;
    buffer2[387] = 30;
    buffer2[388] = 27;
    buffer2[389] = 65;
    buffer2[390] = 71;
    buffer2[391] = 127;
    buffer2[392] = 48;
    buffer2[393] = 31;
    buffer2[394] = 7;
    buffer2[395] = 1;
    buffer2[396] = 27;
    buffer2[397] = 29;
    buffer2[398] = 23;
    buffer2[399] = 27;
    buffer2[400] = 25;
    buffer2[401] = 0;
    buffer2[402] = 0;
    buffer2[403] = 0;
    buffer2[404] = 0;
    buffer2[405] = 0;
    buffer2[406] = 0;
    buffer2[407] = 0;
    buffer2[408] = 0;
    buffer2[409] = 0;
    buffer2[410] = 0;
    buffer2[411] = 0;
    buffer2[412] = 0;
    buffer2[413] = 0;
    buffer2[414] = 0;
    buffer2[415] = 0;
    buffer2[416] = 0;
    buffer2[417] = 0;
    buffer2[418] = 0;
    buffer2[419] = 0;
    buffer2[420] = 0;
    buffer2[421] = 0;
    buffer2[422] = 0;
    buffer2[423] = 0;
    buffer2[424] = 0;
    buffer2[425] = 0;
    buffer2[426] = 0;
    buffer2[427] = 0;
    buffer2[428] = 0;
    buffer2[429] = 0;
    buffer2[430] = 0;
    buffer2[431] = 0;
    buffer2[432] = 0;
    buffer2[433] = 0;
    buffer2[434] = 0;
    buffer2[435] = 0;
    buffer2[436] = 0;
    buffer2[437] = 0;
    buffer2[438] = 0;
    buffer2[439] = 0;
    buffer2[440] = 0;
    buffer2[441] = 0;
    buffer2[442] = 0;
    buffer2[443] = 0;
    buffer2[444] = 0;
    buffer2[445] = 0;
    buffer2[446] = 0;
    buffer2[447] = 0;
    buffer2[448] = 0;
    buffer2[449] = 0;
    buffer2[450] = 0;
    buffer2[451] = 0;
    buffer2[452] = 0;
    buffer2[453] = 0;
    buffer2[454] = 0;
    buffer2[455] = 0;
    buffer2[456] = 0;
    buffer2[457] = 0;
    buffer2[458] = 0;
    buffer2[459] = 0;
    buffer2[460] = 0;
    buffer2[461] = 0;
    buffer2[462] = 0;
    buffer2[463] = 0;
    buffer2[464] = 0;
    buffer2[465] = 0;
    buffer2[466] = 0;
    buffer2[467] = 0;
    buffer2[468] = 0;
    buffer2[469] = 0;
    buffer2[470] = 0;
    buffer2[471] = 0;
    buffer2[472] = 0;
    buffer2[473] = 0;
    buffer2[474] = 0;
    buffer2[475] = 0;
    buffer2[476] = 0;
    buffer2[477] = 0;
    buffer2[478] = 0;
    buffer2[479] = 0;
    buffer2[480] = 0;
    buffer2[481] = 0;
    buffer2[482] = 0;
    buffer2[483] = 0;
    buffer2[484] = 0;
    buffer2[485] = 0;
    buffer2[486] = 0;
    buffer2[487] = 0;
    buffer2[488] = 0;
    buffer2[489] = 0;
    buffer2[490] = 0;
    buffer2[491] = 0;
    buffer2[492] = 0;
    buffer2[493] = 0;
    buffer2[494] = 0;
    buffer2[495] = 0;
    buffer2[496] = 0;
    buffer2[497] = 0;
    buffer2[498] = 0;
    buffer2[499] = 0;
    buffer2[500] = 0;
    buffer2[501] = 0;
    buffer2[502] = 0;
    buffer2[503] = 0;
    buffer2[504] = 0;
    buffer2[505] = 0;
    buffer2[506] = 0;
    buffer2[507] = 0;
    buffer2[508] = 0;
    buffer2[509] = 0;
    buffer2[510] = 0;
    buffer2[511] = 0;
  }
  
  private static int[] buffer3 = new int[512];
  static
  {
    buffer3[0] = 248;
    buffer3[1] = 252;
    buffer3[2] = 4;
    buffer3[3] = 252;
    buffer3[4] = 248;
    buffer3[5] = 0;
    buffer3[6] = 8;
    buffer3[7] = 8;
    buffer3[8] = 252;
    buffer3[9] = 252;
    buffer3[10] = 0;
    buffer3[11] = 0;
    buffer3[12] = 136;
    buffer3[13] = 204;
    buffer3[14] = 100;
    buffer3[15] = 188;
    buffer3[16] = 152;
    buffer3[17] = 0;
    buffer3[18] = 136;
    buffer3[19] = 172;
    buffer3[20] = 36;
    buffer3[21] = 252;
    buffer3[22] = 216;
    buffer3[23] = 0;
    buffer3[24] = 96;
    buffer3[25] = 112;
    buffer3[26] = 72;
    buffer3[27] = 252;
    buffer3[28] = 252;
    buffer3[29] = 64;
    buffer3[30] = 188;
    buffer3[31] = 60;
    buffer3[32] = 20;
    buffer3[33] = 244;
    buffer3[34] = 228;
    buffer3[35] = 0;
    buffer3[36] = 248;
    buffer3[37] = 252;
    buffer3[38] = 36;
    buffer3[39] = 236;
    buffer3[40] = 200;
    buffer3[41] = 0;
    buffer3[42] = 12;
    buffer3[43] = 140;
    buffer3[44] = 228;
    buffer3[45] = 124;
    buffer3[46] = 28;
    buffer3[47] = 0;
    buffer3[48] = 216;
    buffer3[49] = 252;
    buffer3[50] = 36;
    buffer3[51] = 252;
    buffer3[52] = 216;
    buffer3[53] = 0;
    buffer3[54] = 152;
    buffer3[55] = 188;
    buffer3[56] = 36;
    buffer3[57] = 252;
    buffer3[58] = 248;
    buffer3[59] = 0;
    buffer3[60] = 0;
    buffer3[61] = 0;
    buffer3[62] = 0;
    buffer3[63] = 0;
    buffer3[64] = 0;
    buffer3[65] = 0;
    buffer3[66] = 0;
    buffer3[67] = 0;
    buffer3[68] = 0;
    buffer3[69] = 0;
    buffer3[70] = 0;
    buffer3[71] = 0;
    buffer3[72] = 0;
    buffer3[73] = 0;
    buffer3[74] = 0;
    buffer3[75] = 0;
    buffer3[76] = 0;
    buffer3[77] = 0;
    buffer3[78] = 0;
    buffer3[79] = 0;
    buffer3[80] = 0;
    buffer3[81] = 0;
    buffer3[82] = 0;
    buffer3[83] = 0;
    buffer3[84] = 0;
    buffer3[85] = 0;
    buffer3[86] = 0;
    buffer3[87] = 0;
    buffer3[88] = 0;
    buffer3[89] = 0;
    buffer3[90] = 0;
    buffer3[91] = 0;
    buffer3[92] = 0;
    buffer3[93] = 0;
    buffer3[94] = 0;
    buffer3[95] = 0;
    buffer3[96] = 0;
    buffer3[97] = 0;
    buffer3[98] = 0;
    buffer3[99] = 0;
    buffer3[100] = 0;
    buffer3[101] = 0;
    buffer3[102] = 0;
    buffer3[103] = 0;
    buffer3[104] = 0;
    buffer3[105] = 0;
    buffer3[106] = 0;
    buffer3[107] = 0;
    buffer3[108] = 0;
    buffer3[109] = 0;
    buffer3[110] = 0;
    buffer3[111] = 0;
    buffer3[112] = 0;
    buffer3[113] = 0;
    buffer3[114] = 0;
    buffer3[115] = 0;
    buffer3[116] = 0;
    buffer3[117] = 0;
    buffer3[118] = 0;
    buffer3[119] = 0;
    buffer3[120] = 0;
    buffer3[121] = 0;
    buffer3[122] = 0;
    buffer3[123] = 0;
    buffer3[124] = 0;
    buffer3[125] = 0;
    buffer3[126] = 0;
    buffer3[127] = 0;
    buffer3[128] = 128;
    buffer3[129] = 193;
    buffer3[130] = 81;
    buffer3[131] = 241;
    buffer3[132] = 240;
    buffer3[133] = 0;
    buffer3[134] = 129;
    buffer3[135] = 193;
    buffer3[136] = 65;
    buffer3[137] = 193;
    buffer3[138] = 129;
    buffer3[139] = 1;
    buffer3[140] = 65;
    buffer3[141] = 225;
    buffer3[142] = 241;
    buffer3[143] = 81;
    buffer3[144] = 81;
    buffer3[145] = 0;
    buffer3[146] = 128;
    buffer3[147] = 193;
    buffer3[148] = 65;
    buffer3[149] = 129;
    buffer3[150] = 192;
    buffer3[151] = 16;
    buffer3[152] = 240;
    buffer3[153] = 240;
    buffer3[154] = 64;
    buffer3[155] = 193;
    buffer3[156] = 129;
    buffer3[157] = 0;
    buffer3[158] = 65;
    buffer3[159] = 65;
    buffer3[160] = 209;
    buffer3[161] = 209;
    buffer3[162] = 0;
    buffer3[163] = 0;
    buffer3[164] = 64;
    buffer3[165] = 65;
    buffer3[166] = 209;
    buffer3[167] = 209;
    buffer3[168] = 0;
    buffer3[169] = 16;
    buffer3[170] = 240;
    buffer3[171] = 241;
    buffer3[172] = 129;
    buffer3[173] = 192;
    buffer3[174] = 64;
    buffer3[175] = 0;
    buffer3[176] = 16;
    buffer3[177] = 17;
    buffer3[178] = 241;
    buffer3[179] = 241;
    buffer3[180] = 0;
    buffer3[181] = 64;
    buffer3[182] = 192;
    buffer3[183] = 193;
    buffer3[184] = 193;
    buffer3[185] = 193;
    buffer3[186] = 128;
    buffer3[187] = 64;
    buffer3[188] = 192;
    buffer3[189] = 128;
    buffer3[190] = 64;
    buffer3[191] = 192;
    buffer3[192] = 128;
    buffer3[193] = 0;
    buffer3[194] = 128;
    buffer3[195] = 192;
    buffer3[196] = 64;
    buffer3[197] = 192;
    buffer3[198] = 128;
    buffer3[199] = 64;
    buffer3[200] = 192;
    buffer3[201] = 192;
    buffer3[202] = 64;
    buffer3[203] = 192;
    buffer3[204] = 128;
    buffer3[205] = 0;
    buffer3[206] = 128;
    buffer3[207] = 192;
    buffer3[208] = 64;
    buffer3[209] = 128;
    buffer3[210] = 192;
    buffer3[211] = 64;
    buffer3[212] = 64;
    buffer3[213] = 192;
    buffer3[214] = 128;
    buffer3[215] = 192;
    buffer3[216] = 64;
    buffer3[217] = 192;
    buffer3[218] = 128;
    buffer3[219] = 192;
    buffer3[220] = 192;
    buffer3[221] = 64;
    buffer3[222] = 64;
    buffer3[223] = 0;
    buffer3[224] = 64;
    buffer3[225] = 240;
    buffer3[226] = 240;
    buffer3[227] = 64;
    buffer3[228] = 64;
    buffer3[229] = 64;
    buffer3[230] = 192;
    buffer3[231] = 192;
    buffer3[232] = 0;
    buffer3[233] = 192;
    buffer3[234] = 192;
    buffer3[235] = 64;
    buffer3[236] = 192;
    buffer3[237] = 192;
    buffer3[238] = 0;
    buffer3[239] = 192;
    buffer3[240] = 192;
    buffer3[241] = 64;
    buffer3[242] = 192;
    buffer3[243] = 0;
    buffer3[244] = 192;
    buffer3[245] = 0;
    buffer3[246] = 192;
    buffer3[247] = 64;
    buffer3[248] = 0;
    buffer3[249] = 0;
    buffer3[250] = 0;
    buffer3[251] = 0;
    buffer3[252] = 0;
    buffer3[253] = 0;
    buffer3[254] = 0;
    buffer3[255] = 0;
    buffer3[256] = 3;
    buffer3[257] = 7;
    buffer3[258] = 4;
    buffer3[259] = 7;
    buffer3[260] = 7;
    buffer3[261] = 4;
    buffer3[262] = 3;
    buffer3[263] = 7;
    buffer3[264] = 5;
    buffer3[265] = 5;
    buffer3[266] = 5;
    buffer3[267] = 0;
    buffer3[268] = 4;
    buffer3[269] = 7;
    buffer3[270] = 7;
    buffer3[271] = 4;
    buffer3[272] = 4;
    buffer3[273] = 0;
    buffer3[274] = 19;
    buffer3[275] = 23;
    buffer3[276] = 20;
    buffer3[277] = 31;
    buffer3[278] = 15;
    buffer3[279] = 0;
    buffer3[280] = 7;
    buffer3[281] = 7;
    buffer3[282] = 0;
    buffer3[283] = 7;
    buffer3[284] = 7;
    buffer3[285] = 0;
    buffer3[286] = 4;
    buffer3[287] = 4;
    buffer3[288] = 7;
    buffer3[289] = 7;
    buffer3[290] = 4;
    buffer3[291] = 4;
    buffer3[292] = 16;
    buffer3[293] = 16;
    buffer3[294] = 31;
    buffer3[295] = 15;
    buffer3[296] = 0;
    buffer3[297] = 0;
    buffer3[298] = 7;
    buffer3[299] = 7;
    buffer3[300] = 3;
    buffer3[301] = 6;
    buffer3[302] = 4;
    buffer3[303] = 4;
    buffer3[304] = 4;
    buffer3[305] = 4;
    buffer3[306] = 7;
    buffer3[307] = 7;
    buffer3[308] = 4;
    buffer3[309] = 0;
    buffer3[310] = 7;
    buffer3[311] = 0;
    buffer3[312] = 7;
    buffer3[313] = 0;
    buffer3[314] = 7;
    buffer3[315] = 0;
    buffer3[316] = 7;
    buffer3[317] = 7;
    buffer3[318] = 0;
    buffer3[319] = 7;
    buffer3[320] = 7;
    buffer3[321] = 0;
    buffer3[322] = 3;
    buffer3[323] = 7;
    buffer3[324] = 4;
    buffer3[325] = 7;
    buffer3[326] = 3;
    buffer3[327] = 16;
    buffer3[328] = 31;
    buffer3[329] = 31;
    buffer3[330] = 20;
    buffer3[331] = 7;
    buffer3[332] = 3;
    buffer3[333] = 0;
    buffer3[334] = 3;
    buffer3[335] = 7;
    buffer3[336] = 20;
    buffer3[337] = 31;
    buffer3[338] = 31;
    buffer3[339] = 16;
    buffer3[340] = 4;
    buffer3[341] = 7;
    buffer3[342] = 7;
    buffer3[343] = 4;
    buffer3[344] = 0;
    buffer3[345] = 0;
    buffer3[346] = 4;
    buffer3[347] = 5;
    buffer3[348] = 5;
    buffer3[349] = 7;
    buffer3[350] = 7;
    buffer3[351] = 2;
    buffer3[352] = 0;
    buffer3[353] = 3;
    buffer3[354] = 7;
    buffer3[355] = 4;
    buffer3[356] = 6;
    buffer3[357] = 0;
    buffer3[358] = 3;
    buffer3[359] = 7;
    buffer3[360] = 4;
    buffer3[361] = 7;
    buffer3[362] = 7;
    buffer3[363] = 0;
    buffer3[364] = 0;
    buffer3[365] = 3;
    buffer3[366] = 7;
    buffer3[367] = 3;
    buffer3[368] = 0;
    buffer3[369] = 0;
    buffer3[370] = 1;
    buffer3[371] = 7;
    buffer3[372] = 3;
    buffer3[373] = 7;
    buffer3[374] = 3;
    buffer3[375] = 0;
    buffer3[376] = 0;
    buffer3[377] = 0;
    buffer3[378] = 0;
    buffer3[379] = 0;
    buffer3[380] = 0;
    buffer3[381] = 0;
    buffer3[382] = 0;
    buffer3[383] = 0;
    buffer3[384] = 17;
    buffer3[385] = 27;
    buffer3[386] = 15;
    buffer3[387] = 30;
    buffer3[388] = 27;
    buffer3[389] = 65;
    buffer3[390] = 71;
    buffer3[391] = 127;
    buffer3[392] = 48;
    buffer3[393] = 31;
    buffer3[394] = 7;
    buffer3[395] = 1;
    buffer3[396] = 27;
    buffer3[397] = 29;
    buffer3[398] = 23;
    buffer3[399] = 27;
    buffer3[400] = 25;
    buffer3[401] = 0;
    buffer3[402] = 0;
    buffer3[403] = 0;
    buffer3[404] = 0;
    buffer3[405] = 0;
    buffer3[406] = 0;
    buffer3[407] = 0;
    buffer3[408] = 0;
    buffer3[409] = 0;
    buffer3[410] = 0;
    buffer3[411] = 0;
    buffer3[412] = 0;
    buffer3[413] = 0;
    buffer3[414] = 0;
    buffer3[415] = 0;
    buffer3[416] = 0;
    buffer3[417] = 0;
    buffer3[418] = 0;
    buffer3[419] = 0;
    buffer3[420] = 0;
    buffer3[421] = 0;
    buffer3[422] = 0;
    buffer3[423] = 0;
    buffer3[424] = 0;
    buffer3[425] = 0;
    buffer3[426] = 0;
    buffer3[427] = 0;
    buffer3[428] = 0;
    buffer3[429] = 0;
    buffer3[430] = 0;
    buffer3[431] = 0;
    buffer3[432] = 0;
    buffer3[433] = 0;
    buffer3[434] = 0;
    buffer3[435] = 0;
    buffer3[436] = 0;
    buffer3[437] = 0;
    buffer3[438] = 0;
    buffer3[439] = 0;
    buffer3[440] = 0;
    buffer3[441] = 0;
    buffer3[442] = 0;
    buffer3[443] = 0;
    buffer3[444] = 0;
    buffer3[445] = 0;
    buffer3[446] = 0;
    buffer3[447] = 0;
    buffer3[448] = 0;
    buffer3[449] = 0;
    buffer3[450] = 0;
    buffer3[451] = 0;
    buffer3[452] = 0;
    buffer3[453] = 0;
    buffer3[454] = 0;
    buffer3[455] = 0;
    buffer3[456] = 0;
    buffer3[457] = 0;
    buffer3[458] = 0;
    buffer3[459] = 0;
    buffer3[460] = 0;
    buffer3[461] = 0;
    buffer3[462] = 0;
    buffer3[463] = 0;
    buffer3[464] = 0;
    buffer3[465] = 0;
    buffer3[466] = 0;
    buffer3[467] = 0;
    buffer3[468] = 0;
    buffer3[469] = 0;
    buffer3[470] = 0;
    buffer3[471] = 0;
    buffer3[472] = 0;
    buffer3[473] = 0;
    buffer3[474] = 0;
    buffer3[475] = 0;
    buffer3[476] = 0;
    buffer3[477] = 0;
    buffer3[478] = 0;
    buffer3[479] = 0;
    buffer3[480] = 0;
    buffer3[481] = 0;
    buffer3[482] = 0;
    buffer3[483] = 0;
    buffer3[484] = 0;
    buffer3[485] = 0;
    buffer3[486] = 0;
    buffer3[487] = 0;
    buffer3[488] = 0;
    buffer3[489] = 0;
    buffer3[490] = 0;
    buffer3[491] = 0;
    buffer3[492] = 0;
    buffer3[493] = 0;
    buffer3[494] = 0;
    buffer3[495] = 0;
    buffer3[496] = 0;
    buffer3[497] = 0;
    buffer3[498] = 0;
    buffer3[499] = 0;
    buffer3[500] = 0;
    buffer3[501] = 0;
    buffer3[502] = 0;
    buffer3[503] = 0;
    buffer3[504] = 0;
    buffer3[505] = 0;
    buffer3[506] = 0;
    buffer3[507] = 0;
    buffer3[508] = 0;
    buffer3[509] = 0;
    buffer3[510] = 0;
    buffer3[511] = 0;
  }
  
  /** Creates new form ImageBuilder */
  public ImageBuilder()
  {
    initComponents();
    this.setSize(new Dimension(1000, 300));
  }

  /** 
   * This method is called from within the constructor to
   * initialize the form.
   */
  private void initComponents()
  {
    if (nbCols == -1)
      ledPanel = new LEDPanel();
    else
      ledPanel = new LEDPanel(nbCols);

    ledPanel.setWithGrid(false);
    
    setPreferredSize(new java.awt.Dimension(1000, 600));
    setTitle("OLED Screen Buffer");
    addWindowListener(new java.awt.event.WindowAdapter()
    {
      public void windowClosing(java.awt.event.WindowEvent evt)
      {
        exitForm(evt);
      }
    });
    add(ledPanel, java.awt.BorderLayout.CENTER);
    
    bottomPanel = new JPanel();
    gridCheckBox = new JCheckBox("With Grid");
    gridCheckBox.setSelected(false);
    bottomPanel.add(gridCheckBox, null);
    gridCheckBox.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent actionEvent)
      {
        ledPanel.setWithGrid(gridCheckBox.isSelected());
        ledPanel.repaint();
      }
    });
    againButton = new JButton("Play again");
    bottomPanel.add(againButton, null);
    againButton.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent actionEvent)
      {
        Thread go = new Thread()
          {
            public void run()
            {
              instance.doYourJob();    
            }
          };
        go.start();
      }
    });
    
    add(bottomPanel, java.awt.BorderLayout.SOUTH);
    pack();
  }

  /**
   * Simulator. Takes the screenbuffer expected by the real device and displays it on
   * a led array (2 dims).
   * 
   * @param screenbuffer as expected by the device.
   */
  private void setBuffer(int[] screenbuffer)
  {
    // This displays the buffer top to bottom, instead of left to right
    char[][] screenMatrix = new char[32][128];
    for (int i=0; i<(buffer.length / 4); i++)
    {
      String line = lpad(Integer.toBinaryString(screenbuffer[i + (3 * 128)]), "0", 8).replace('0', ' ').replace('1', 'X') + // " " + 
                    lpad(Integer.toBinaryString(screenbuffer[i + (2 * 128)]), "0", 8).replace('0', ' ').replace('1', 'X') + // " " + 
                    lpad(Integer.toBinaryString(screenbuffer[i + (1 * 128)]), "0", 8).replace('0', ' ').replace('1', 'X') + // " " + 
                    lpad(Integer.toBinaryString(screenbuffer[i + (0 * 128)]), "0", 8).replace('0', ' ').replace('1', 'X');
      for (int c=0; c<line.length(); c++)
        screenMatrix[c][i] = line.charAt(c);
    //    System.out.println(line);
    }
    // Display the screen matrix, as it should be seen
    boolean[][] matrix = ledPanel.getLedOnOff();
    for (int i=0; i<32; i++)
 // for (int i=31; i>=0; i--)
    {
      for (int j=0; j<128; j++)
        matrix[j][31 - i] = (screenMatrix[i][j] == 'X' ? true : false);
    }    
    ledPanel.setLedOnOff(matrix);
  }
  
  private void display()
  {
    ledPanel.repaint();
  }

  @SuppressWarnings("oracle.jdeveloper.java.insufficient-catch-block")
  public void doYourJob()
  {
    ImageBuilder oled = instance;
    againButton.setEnabled(false);
//  instance.repaint();

    if (false)
      oled.setBuffer(buffer); // Static buffer, for tests
    if (false)
      oled.setBuffer(buffer1); // Static buffer, for font (part 1)
    if (false)
      oled.setBuffer(buffer2); // Static buffer, for font (part 2)
    if (false)
      oled.setBuffer(buffer3); // Static buffer, for font (part 3)
    
    if (true)
    {
      if (sb == null)
      {
        sb = new ScreenBuffer(128, 32);
        sb.clear(ScreenBuffer.Mode.BLACK_ON_WHITE);
      }

      if (true)
      {
        sb.text("ScreenBuffer",      2,  9, ScreenBuffer.Mode.BLACK_ON_WHITE);
        sb.text("128 x 32 for OLED", 2, 19, ScreenBuffer.Mode.BLACK_ON_WHITE);
        sb.text("I speak Java!",     2, 29, ScreenBuffer.Mode.BLACK_ON_WHITE);
        oled.setBuffer(sb.getScreenBuffer());
        oled.display();
//      sb.dumpScreen();
        try { Thread.sleep(2000); } catch (Exception ex) {}

        // Blinking
        sb.clear(ScreenBuffer.Mode.WHITE_ON_BLACK);
        sb.text("ScreenBuffer",      2,  9, ScreenBuffer.Mode.WHITE_ON_BLACK);
        sb.text("128 x 32 for OLED", 2, 19, ScreenBuffer.Mode.WHITE_ON_BLACK);
        sb.text("I speak Java!",     2, 29, ScreenBuffer.Mode.WHITE_ON_BLACK);
        oled.setBuffer(sb.getScreenBuffer());
        oled.display();
        try { Thread.sleep(50); } catch (Exception ex) {}
        
        sb.clear(ScreenBuffer.Mode.BLACK_ON_WHITE);
        sb.text("ScreenBuffer",      2,  9, ScreenBuffer.Mode.BLACK_ON_WHITE);
        sb.text("128 x 32 for OLED", 2, 19, ScreenBuffer.Mode.BLACK_ON_WHITE);
        sb.text("I speak Java!",     2, 29, ScreenBuffer.Mode.BLACK_ON_WHITE);
        oled.setBuffer(sb.getScreenBuffer());
        oled.display();
        try { Thread.sleep(50); } catch (Exception ex) {}
        
        sb.clear(ScreenBuffer.Mode.WHITE_ON_BLACK);
        sb.text("ScreenBuffer",      2,  9, ScreenBuffer.Mode.WHITE_ON_BLACK);
        sb.text("128 x 32 for OLED", 2, 19, ScreenBuffer.Mode.WHITE_ON_BLACK);
        sb.text("I speak Java!",     2, 29, ScreenBuffer.Mode.WHITE_ON_BLACK);
        oled.setBuffer(sb.getScreenBuffer());
        oled.display();
        try { Thread.sleep(50); } catch (Exception ex) {}
        
        sb.clear(ScreenBuffer.Mode.BLACK_ON_WHITE);
        sb.text("ScreenBuffer",      2,  9, ScreenBuffer.Mode.BLACK_ON_WHITE);
        sb.text("128 x 32 for OLED", 2, 19, ScreenBuffer.Mode.BLACK_ON_WHITE);
        sb.text("I speak Java!",     2, 29, ScreenBuffer.Mode.BLACK_ON_WHITE);
        oled.setBuffer(sb.getScreenBuffer());
        oled.display();
        try { Thread.sleep(50); } catch (Exception ex) {}

        sb.clear(ScreenBuffer.Mode.WHITE_ON_BLACK);
        sb.text("ScreenBuffer",      2,  9, ScreenBuffer.Mode.WHITE_ON_BLACK);
        sb.text("128 x 32 for OLED", 2, 19, ScreenBuffer.Mode.WHITE_ON_BLACK);
        sb.text("I speak Java!",     2, 29, ScreenBuffer.Mode.WHITE_ON_BLACK);
        oled.setBuffer(sb.getScreenBuffer());
        oled.display();
        try { Thread.sleep(2000); } catch (Exception ex) {}
        
        // End blinking    
      }
      
      if (false)
      {
        String[] txt1 = new String[] {
            "!\":#$%&'()*+,-./01234",
            "56789;<=>?@ABCDEFGHI",
            "JKLMNOPQRSTUVWXYZ[\\]"
        };
        String[] txt2 = new String[] {
            "^_abcdefghijklmnopqr",
            "stuvwxyz{|}"
        };
  
        boolean one = false;
        
        for (int t=0; t<4; t++)
        {
          sb.clear();
          String[] sa = one ? txt1 : txt2;
          for (int i=0; i<sa.length; i++)
            sb.text(sa[i], 0, 10 + (i * 10));
          oled.setBuffer(sb.getScreenBuffer());
          oled.display();
          one = !one;
          try { Thread.sleep(2000); } catch (Exception ex) {}
        }
      }
      
      // Image + text, marquee
      if (false)
      {
        sb.clear(ScreenBuffer.Mode.BLACK_ON_WHITE);
        ImgInterface img = new Java32x32(); 
        sb.image(img, 0, 0, ScreenBuffer.Mode.BLACK_ON_WHITE);
        sb.text("I speak Java!", 36, 20, ScreenBuffer.Mode.BLACK_ON_WHITE);
      
        oled.setBuffer(sb.getScreenBuffer());
        oled.display();
        try { Thread.sleep(2000); } catch (Exception ex) {}
        
        sb.clear();
        for (int x=0; x<128; x++)
        {
          sb.image(img, 0 - x, 0);
          sb.text("I speak Java!.....", 36 - x, 20);
        
          oled.setBuffer(sb.getScreenBuffer());
          oled.display();
          long s = (long)(150 - (1.5 * x));
          try { Thread.sleep(s > 0 ? s : 0); } catch (Exception ex) {}
        }
      }
      
      // Circles
      if (false)
      {
        sb.clear();
        sb.circle(64, 16, 15);
        oled.setBuffer(sb.getScreenBuffer());          
        oled.display();
        try { Thread.sleep(1000); } catch (Exception ex) {}
        
        sb.circle(74, 16, 10);
        oled.setBuffer(sb.getScreenBuffer());          
        oled.display();
        try { Thread.sleep(1000); } catch (Exception ex) {}
        
        sb.circle(80, 16,  5);
        oled.setBuffer(sb.getScreenBuffer());          
        oled.display();
        try { Thread.sleep(1000); } catch (Exception ex) {}          
      }
      
      // Lines
      if (false)
      {
        sb.clear();
        sb.line(1, 1, 126, 30);
        oled.setBuffer(sb.getScreenBuffer());          
        oled.display();
        try { Thread.sleep(1000); } catch (Exception ex) {}
        
        sb.line(126, 1, 1, 30);
        oled.setBuffer(sb.getScreenBuffer());          
        oled.display();
        try { Thread.sleep(1000); } catch (Exception ex) {}
        
        sb.line(1, 25, 120, 10);
        oled.setBuffer(sb.getScreenBuffer());          
        oled.display();
        try { Thread.sleep(1000); } catch (Exception ex) {}
        
        sb.line(10, 5, 10, 30);
        oled.setBuffer(sb.getScreenBuffer());          
        oled.display();
        try { Thread.sleep(1000); } catch (Exception ex) {}
        
        sb.line(1, 5, 120, 5);
        oled.setBuffer(sb.getScreenBuffer());          
        oled.display();
        try { Thread.sleep(1000); } catch (Exception ex) {}          
      }
      
      // Rectangle
      if (false)
      {
        sb.clear();
        sb.rectangle(5, 10, 100, 25);
        oled.setBuffer(sb.getScreenBuffer());          
        oled.display();
        try { Thread.sleep(1000); } catch (Exception ex) {}

        sb.rectangle(15, 3, 50, 30);
        oled.setBuffer(sb.getScreenBuffer());          
        oled.display();
        try { Thread.sleep(1000); } catch (Exception ex) {}
      }
      
      // Nested rectangles
      if (true)
      {
        sb.clear();
        for (int i=0; i<8; i++)
        {
          sb.rectangle(1 + (i*2), 1 + (i*2), 127 - (i*2), 31 - (i*2));
          oled.setBuffer(sb.getScreenBuffer());          
          oled.display();
          try { Thread.sleep(100); } catch (Exception ex) {}
        }
        try { Thread.sleep(1000); } catch (Exception ex) {}
      }
      
      // Arc
      if (false)
      {
        sb.clear();
        sb.arc(64, 16, 10, 20, 90);
        sb.plot(64, 16);
        oled.setBuffer(sb.getScreenBuffer());          
        oled.display();
        try { Thread.sleep(1000); } catch (Exception ex) {}
      }
      
      // Shape
      if (true)
      {
        sb.clear();
        int[] x = new int[] { 64, 73, 50, 78, 55 };
        int[] y = new int[] {  1, 30, 12, 12, 30 };
        Polygon p = new Polygon(x, y, 5);
        sb.shape(p, true);
        oled.setBuffer(sb.getScreenBuffer());          
        oled.display();
        try { Thread.sleep(1000); } catch (Exception ex) {}
      }
      
      // Centered text
      if (true)
      {
        sb.clear();
        String txt = "Centered";
        int len = sb.strlen(txt);
        sb.text(txt, 64 - (len/2), 16);
        oled.setBuffer(sb.getScreenBuffer());          
        oled.display();
        try { Thread.sleep(1000); } catch (Exception ex) {}
     // sb.clear();
        txt = "A much longer string.";
        len = sb.strlen(txt);
        sb.text(txt, 64 - (len/2), 26);
        oled.setBuffer(sb.getScreenBuffer());          
        oled.display();
        try { Thread.sleep(1000); } catch (Exception ex) {}
      }
      
      // Vertical marquee
      if (true)
      {
        String[] txt = new String[] {
          "Centered",
          "This is line one",
          "More text goes here",
          "Some crap follows: ...", 
          "We're reaching the end",
          "* The End *"
        };
        int len = 0;
        for (int t=0; t<80; t++)
        {
          sb.clear();
          for (int i=0; i<txt.length; i++)
          {
            len = sb.strlen(txt[i]);
            sb.text(txt[i], 64 - (len/2), (10 * (i+1)) - t);
            oled.setBuffer(sb.getScreenBuffer());          
            oled.display();
          }
          try { Thread.sleep(100); } catch (Exception ex) {}
        }
//      sb.dumpScreen();
        
        try { Thread.sleep(1000); } catch (Exception ex) {}
      }
      
      if (true)
      {
        // Text Snake...
        String snake = "This text is displayed like a snake, waving across the screen...";
        char[] ca = snake.toCharArray();
        int strlen = sb.strlen(snake);
     // int i = 0;
        for (int i=0; i<strlen + 2; i++)
        {
          sb.clear();
          for (int c=0; c<ca.length; c++)
          {
            int strOffset = 0;
            if (c > 0)
            {
              String tmp = new String(ca, 0, c);
        //    System.out.println(tmp);
              strOffset = sb.strlen(tmp) + 2;
            }
            double virtualAngle = Math.PI * (((c - i) % 32) / 32d);
            int x = strOffset - i,
                y = 26 + (int)(16 * Math.sin(virtualAngle)); 
//          System.out.println("Displaying " + ca[c] + " at " + x + ", " + y + ", i=" + i + ", strOffset=" + strOffset);
            sb.text(new String(new char[] { ca[c] }), x, y);             
          }
          oled.setBuffer(sb.getScreenBuffer());          
          oled.display();
          try { Thread.sleep(75); } catch (Exception ex) {}
        }        
      }
      
      // Curve      
      if (true)
      {
        sb.clear();
        // Axis
        sb.line(0, 16, 128, 16);
        sb.line(2, 0, 2, 32);
        
        Point prev = null;
        for (int x=0; x<130; x++)
        {
          double amplitude = 6 * Math.exp((double)(130 - x) / (13d * 7.5d)); 
    //    System.out.println("X:" + x + ", ampl: " + (amplitude));
          int y = 16 - (int)(amplitude * Math.cos(Math.toRadians(360 * x / 16d)));
          sb.plot(x + 2, y);
          if (prev != null)
            sb.line(prev.x, prev.y, x+2, y);
          prev = new Point(x+2, y);
        }
        oled.setBuffer(sb.getScreenBuffer());          
        oled.display();        
        try { Thread.sleep(1000); } catch (Exception ex) {}
      }
      
      // Progressing Curve      
      if (true)
      {
        sb.clear();
        // Axis
        sb.line(0, 16, 128, 16);
        sb.line(2, 0, 2, 32);
        
        Point prev = null;
        for (int x=0; x<130; x++)
        {
          double amplitude = 6 * Math.exp((double)(130 - x) / (13d * 7.5d)); 
      //  System.out.println("X:" + x + ", ampl: " + (amplitude));
          int y = 16 - (int)(amplitude * Math.cos(Math.toRadians(360 * x / 16d)));
          sb.plot(x + 2, y);
          if (prev != null)
            sb.line(prev.x, prev.y, x+2, y);
          prev = new Point(x+2, y);
          oled.setBuffer(sb.getScreenBuffer());          
          oled.display();        
          try { Thread.sleep(75); } catch (Exception ex) {}
        }
        oled.setBuffer(sb.getScreenBuffer());          
        oled.display();        
        try { Thread.sleep(1000); } catch (Exception ex) {}
      }

      // Bouncing      
      if (true)
      {
        sb.clear();
        for (int x=0; x<130; x++)
        {
          sb.clear();
          double amplitude = 6 * Math.exp((double)(130 - x) / (13d * 7.5d)); 
      //  System.out.println("X:" + x + ", ampl: " + (amplitude));
          int y = 32 - (int)(amplitude * Math.abs(Math.cos(Math.toRadians(180 * x / 10d))));
          // 4 dots
          sb.plot(x,   y);
          sb.plot(x+1, y);
          sb.plot(x+1, y+1);
          sb.plot(x,   y+1);

          oled.setBuffer(sb.getScreenBuffer());          
          oled.display();        
          try { Thread.sleep(75); } catch (Exception ex) {}
        }
    //  oled.setBuffer(sb.getScreenBuffer());          
    //  oled.display();        
        try { Thread.sleep(1000); } catch (Exception ex) {}
      }
      againButton.setEnabled(true);

      System.out.println("...Done!");
    }
  }
  
  private static String lpad(String str, String with, int len)
  {
    String s = str;
    while (s.length() < len)
      s = with + s;
    return s;
  }

  /** Exit the Application */
  private void exitForm(@SuppressWarnings("oracle.jdeveloper.java.unused-parameter") java.awt.event.WindowEvent evt) 
  {
    System.exit(0);
  }

  /**
   * @param args the command line arguments
   */
  public static void main(String args[])
  {
    // Available characters:
    Map<String, String[]> characters = CharacterMatrixes.characters;
    Set<String> keys = characters.keySet();
    List<String> kList = new ArrayList<String>(keys.size());
    for (String k : keys)
      kList.add(k);
    // Sort here
    Collections.sort(kList);
    for (String k : kList)
      System.out.print(k + " ");
    System.out.println();
    
    // Params
    if (args.length > 0)
    {
      for (int i=0; i<args.length; i++)
      {
        if ("-col".equals(args[i]))
          nbCols = Integer.parseInt(args[i+1]);
      }
    }

    ImageBuilder ib = new ImageBuilder();
    ib.setVisible(true);
    ib.doYourJob();
  }
}
