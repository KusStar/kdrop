const { generate } = require('generate-passphrase')

exports.generatePasspharase = () => generate({ length: 2 })

exports.FILE_SEPARTOR = '-kdrop-'