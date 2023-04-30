import { decryptFile } from "@/utils/crypto";
import { encryptFile } from "@/utils/crypto";
import { type NextPage } from "next";
import Head from "next/head";
import Image from "next/image";
import { useRef } from "react";

const API_URL = process.env.NODE_ENV === 'production' ? 'https://kdrop-api.uselessthing.top/api' : 'http://localhost:3000/api'

const Home: NextPage = () => {
  const uploadRef = useRef<HTMLInputElement>(null)

  const handleUpload = () => {
    uploadRef.current?.click()
  }

  const onFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (!file) return
    console.log('original', file)

    const secret = prompt('Enter secret key')

    if (!secret) return

    const encryptedFile = await encryptFile(file, secret)

    const form = new FormData()
    form.append('file', encryptedFile)

    const res = await fetch(`${API_URL}/upload`, {
      method: 'POST',
      body: form
    })
    // eslint-disable-next-line @typescript-eslint/no-unsafe-assignment
    const data = await res.json()
    console.log(data)
  }

  const onDownload = async () => {
    const token = prompt('Enter download token')

    if (!token) return

    const res = await fetch(`${API_URL}/download/${token}`)

    const contentDisposition = res.headers.get('content-disposition')

    if (contentDisposition) {
      let filename = contentDisposition.split(';')
        .find(n => n.includes('filename='))
        ?.replace('filename=', '')
        .trim() || 'file'

      filename = decodeURI(filename)

      const blob = await res.blob()
      const file = new File([blob], filename, { type: blob.type });

      const secret = prompt('Enter secret key')

      const decryptedFile = await decryptFile(file, secret || 'secret')

      const url = window.URL.createObjectURL(decryptedFile)
      const link = document.createElement('a')
      link.href = url
      link.setAttribute('download', filename)
      document.body.appendChild(link)
      link.click()
      link.remove()
    }
  }

  return (
    <>
      <Head>
        <title>KDrop</title>
        <meta name="description" content="Generated by create-t3-app" />
        <link rel="icon" href="/logo.png" />
      </Head>
      <main className="flex min-h-screen flex-col items-center justify-center bg-gradient-to-b from-[#96f364] to-[#15162c]">
        <div className="container flex flex-col items-center justify-center gap-12 px-4 py-16 ">
          <button
            className="bg-[#15162c] text-[#a7f974] px-4 py-2 rounded-md shadow-md hover:bg-[#a7f974] hover:text-[#15162c] transition duration-300 ease-in-out"
            onClick={handleUpload}
          >
            Upload
          </button>
          <button
            className="bg-[#15162c] text-[#a7f974] px-4 py-2 rounded-md shadow-md hover:bg-[#a7f974] hover:text-[#15162c] transition duration-300 ease-in-out"
            onClick={onDownload}
          >
            Download
          </button>
          <input
            type="file"
            className="hidden"
            onChange={onFileChange}
            ref={uploadRef}
          />
          <Image
            src="/logo.png"
            alt="KDrop Logo"
            width={200}
            height={200}
            className="absolute bottom-12"
          />
        </div>
      </main>
    </>
  );
};

export default Home;
