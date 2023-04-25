/**
 * 这是一个使用 Koa 和 @koa/multer 中间件处理文件上传的服务。
 * 它使用 @koa/multer 来获取上传的文件，并将文件存储到 ./uploads 文件夹中
 * 使用 SQLite 数据库来存储上传的文件元信息，并提供 /download/:secret 接口来通过密钥口令下载加密文件。
 * 加密/解密的操作与服务器无关，由客户端完成。
 */

const Koa = require('koa');
const Router = require('@koa/router');
const multer = require('@koa/multer');
const fs = require('fs')
const path = require('path')

const { db } = require('./db');
const { generatePasspharase, FILE_SEPARTOR } = require('./utils');

const app = new Koa();
const router = new Router();

// 上传文件存储路径，相对于当前的 /uploads 目录中
const storagePath = path.join(__dirname, 'uploads');
if (!fs.existsSync(storagePath)) {
  fs.mkdirSync(storagePath);
}

// 文件存储配置
const diskStorage = multer.diskStorage({
  destination: storagePath,
  filename: (req, file, cb) => {
    cb(null, file.originalname);
  }
})

// 初始化 multer 中间件
const upload = multer({
  storage: diskStorage,
});

// 查询已上传的加密文件列表
router.get('/', async ctx => {
  await new Promise((resolve, reject) => {
    db.all('SELECT * FROM files', (err, rows) => {
      if (err) {
        reject(err)
      } else {
        resolve(rows)
      }
    })
  }).then(rows => {
    ctx.status = 200
    ctx.bodyss = rows
  }).catch(err => {
    ctx.status = 500
    ctx.body = err
  })
})

// 通过 secret 下载加密文件
router.get('/download/:secret', async ctx => {
  const { secret } = ctx.params
  const row = await new Promise((resolve, reject) => {
    db.get('SELECT * FROM files WHERE secret = ?', secret, (err, row) => {
      if (err) {
        reject(err)
      } else {
        resolve(row)
      }
    })
  })
  if (row) {
    ctx.status = 200
    ctx.set('Content-Disposition', `attachment; filename=${row.name}`)
    ctx.body = fs.createReadStream(path.join(__dirname, row.path))
  } else {
    ctx.status = 404
    ctx.body = 'Not Found'
  }
})

// 使用 multer 中间件接受处理上传的加密文件
router.post(
  '/upload',
  upload.single('file'),
  ctx => {
    const secret = generatePasspharase()
    const file = ctx.file
    const filename = file.originalname.split(FILE_SEPARTOR)[1];

    // 数据库插入记录
    db.prepare(`
      INSERT INTO files (secret, name, size, type, path)
      VALUES (?, ?, ?, ?, ?)
    `).run(
      secret,
      filename,
      ctx.file.size,
      ctx.file.mimetype,
      path.relative(__dirname, ctx.file.path)
    )

    console.log(new Date().toLocaleString(), 'upload', filename, 'done')

    ctx.body = {
      status: 'ok',
      msg: `upload ${ctx.file.filename} done`,
      data: {
        file: {
          name: ctx.file.originalname,
          size: ctx.file.size,
          type: ctx.file.mimetype,
        },
        secret,
      }
    };
  }
);

// add the router to our app
app.use(router.routes());
app.use(router.allowedMethods());

console.log('Server running on port 3000')
if (process.env.NODE_ENV === 'development') {
  console.log('http://localhost:3000')
}
// start the server
app.listen(3000);