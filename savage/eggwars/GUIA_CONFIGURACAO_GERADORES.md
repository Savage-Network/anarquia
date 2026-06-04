# Guia de Configuração dos Geradores - EggWars

## Como Configurar os Geradores no Mapa

Para configurar os geradores de recursos no mapa do EggWars, você precisa usar **placas de sinalização** com os nomes específicos.

### Passo a Passo:

1. **Coloque uma placa** (qualquer tipo: `OAK_SIGN`, `SPRUCE_SIGN`, etc.) no local onde você quer que o gerador apareça.

2. **Na primeira linha da placa**, escreva o nome do gerador seguindo o padrão:
   - `[brick_1]` - Gerador de Tijolo #1
   - `[brick_2]` - Gerador de Tijolo #2
   - `[iron_1]` - Gerador de Ferro #1
   - `[iron_2]` - Gerador de Ferro #2
   - `[gold_1]` - Gerador de Ouro #1
   - `[gold_2]` - Gerador de Ouro #2
   - `[diamond_1]` - Gerador de Diamante #1
   - `[diamond_2]` - Gerador de Diamante #2
   - E assim por diante...

3. **Execute o comando** `/bwprocess <mundo> <modo> <build_limit>` para processar o mapa.

### Exemplo de Placa:

```
[brick_1]
```

### Tipos de Geradores Disponíveis:

- **Tijolo (BRICK)**: Gera `CLAY_BRICK` a cada 2 segundos
- **Ferro (IRON)**: Gera `IRON_INGOT` a cada 3 segundos
- **Ouro (GOLD)**: Gera `GOLD_INGOT` a cada 5 segundos
- **Diamante (DIAMOND)**: Gera `DIAMOND` a cada 30 segundos

### Nomenclatura das Placas:

Você pode usar os seguintes formatos na placa:

- `[brick_1]`, `[brick_2]`, `[brick_3]`, `[brick_4]`
- `[iron_1]`, `[iron_2]`, `[iron_3]`, `[iron_4]`
- `[gold_1]`, `[gold_2]`, `[gold_3]`, `[gold_4]`
- `[diamond_1]`, `[diamond_2]`, `[diamond_3]`, `[diamond_4]`

### Configuração do Ovo do Dragão:

Para configurar o ovo do dragão de cada time, coloque uma placa com:

- `[red_egg]` - Ovo do time vermelho
- `[blue_egg]` - Ovo do time azul
- `[yellow_egg]` - Ovo do time amarelo
- E assim por diante para cada cor de time...

**OU** use o formato antigo (compatibilidade):
- `[red_bed]` - Também funciona como ovo do time vermelho

### Localização dos Geradores:

Os geradores podem ser colocados em qualquer lugar do mapa:
- **Geradores de Tijolo, Ferro, Ouro**: Geralmente próximos às ilhas dos times
- **Geradores de Diamante**: Geralmente no meio do mapa ou em ilhas centrais

### Após Processar:

Depois de executar o comando `/bwprocess`, os geradores serão criados automaticamente nas localizações das placas. Cada gerador terá:
- Um **holograma** mostrando o nível, tipo de recurso, quantidade dropada e tempo de geração
- Um **bloco animado** (cabeça do tipo de recurso)
- **Interação**: Clique com botão direito no holograma para abrir o menu de evolução

### Menu de Evolução:

Ao clicar com botão direito no holograma do gerador, você pode:
- Ver os níveis disponíveis
- Evoluir o gerador usando diamantes
- Aumentar a velocidade de geração
- Aumentar a quantidade de recursos gerados

### Dicas:

1. Coloque múltiplos geradores do mesmo tipo em diferentes locais para melhor distribuição
2. Geradores de diamante são mais raros e valiosos - coloque-os em locais estratégicos
3. Os geradores podem ser evoluídos durante o jogo pelos jogadores
4. Cada gerador tem um limite de itens dropados simultaneamente (stack size)
