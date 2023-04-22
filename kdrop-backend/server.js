const Koa = require('koa');
const Router = require('@koa/router');
const multer = require('@koa/multer');
const fs = require('fs')
const path = require('path')

const { db } = require('./db');
const { generatePasspharase } = require('./utils');

const app = new Koa();
const router = new Router();

const storagePath = path.join(__dirname, 'uploads');

if (!fs.existsSync(storagePath)) {
  fs.mkdirSync(storagePath);
}

const diskStorage = multer.diskStorage({
  destination: storagePath,
  filename: (req, file, cb) => {
    cb(null, file.originalname);
  }
})

const upload = multer({
  storage: diskStorage,
});

router.post(
  '/upload',
  upload.single('file'),
  ctx => {
    const secret = generatePasspharase()
    db.prepare(`
      INSERT INTO files (secret, name, size, type, path)
      VALUES (@secret, @name, @size, @type, @path)
    `).run({
      secret,
      name: ctx.file.originalname,
      size: ctx.file.size,
      type: ctx.file.mimetype,
      path: path.relative(__dirname, ctx.file.path)
    })

    console.log(new Date().toLocaleString(), 'upload', ctx.file.filename, 'done')
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

// start the server
app.listen(3000);